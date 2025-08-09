import time
from constants import TOPIC_NAME_USER_EVENTS, CONSUMER_GROUP_ID_AUDIT, KAFKA_BOOTSTRAP_SERVERS, UserEvent
from utils import validate_events, get_kafka_consumer, get_mongo_client
import os
from redis import Redis
from datetime import datetime, timezone
from typing import TypedDict
from pydantic import BaseModel, Field, HttpUrl
from typing import Literal, Union, Optional
from pymongo import  ASCENDING
from pymongo.errors import BulkWriteError




class SuccessfulEvent(BaseModel):
    doc_type: Literal["success"] = "success"
    timestamp: datetime
    user_id: str = Field(min_length=1)
    event_type: str = Field(min_length=1)         # e.g., "page_view"
    page_url: str = Field(min_length=1)           # keep as string if you don't want strict URL validation
    session_id: str = Field(min_length=1)

class FailedEvent(BaseModel):
    doc_type: Literal["failed"] = "failed"
    failed_event: str = Field(min_length=1)       # raw JSON string that failed parsing/validation
    reason: Optional[str] = ""
    timestamp: datetime


EventDoc = Union[SuccessfulEvent, FailedEvent]

def _to_str(val) -> str:
    return val.decode("utf-8", errors="replace") if isinstance(val, (bytes, bytearray)) else str(val)

def insert_event(event: EventDoc):
    payload = event.model_dump()
    result = col.insert_one(payload)
    return result.inserted_id

if __name__ == '__main__':

    mongo_uri = os.getenv("MONGODB_URI", "mongodb://localhost:27017")
    mongo_db_name = os.getenv("MONGO_DB", "analytics")
    mongo_collection_name = os.getenv("MONGO_COLLECTION", "events")

    print("Waiting for kafka to initialise")
    time.sleep(10)

    kafka_bootstrap_server = os.getenv('KAFKA_BOOTSTRAP_SERVERS', KAFKA_BOOTSTRAP_SERVERS)
    consumer_group_id = os.getenv('CONSUMER_GROUP_ID', CONSUMER_GROUP_ID_AUDIT)


    print(f"Connecting to kafka server: {kafka_bootstrap_server} to topic {TOPIC_NAME_USER_EVENTS} consumer group: {consumer_group_id}", )
    consumer = get_kafka_consumer(kafka_bootstrap_server, TOPIC_NAME_USER_EVENTS, consumer_group_id)

    
    print(f"Connecting to mongo db: {mongo_uri} name: {mongo_db_name} collection: {mongo_collection_name}", )
    client = get_mongo_client(mongo_uri)
    col = client[mongo_db_name][mongo_collection_name]

    col.create_index([("doc_type", ASCENDING)])
    col.create_index([("timestamp", ASCENDING)]) 
    col.create_index([("user_id", ASCENDING), ("timestamp", ASCENDING)], sparse=True)



    while True:
        print("Polling for new message")
        # Poll messages for a short duration (non-blocking)
        messages = consumer.poll(timeout_ms=1000, max_records=500)
        docs_to_insert = []

        for tp, records in messages.items():
            for record in records:
                try: 
                    user_event = validate_events(record.value)
                    docs_to_insert.append({
                        "doc_type": "success",
                        **user_event  # user_event already validated and contains timestamp etc.
                    })
                except ValueError as e:
                    print("Invalid event: ", record.value)
                    docs_to_insert.append({
                        "doc_type": "failed",
                        "timestamp": datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
                        "failed_event": _to_str(record.value),
                        "reason": str(e),
                    })
                
        if docs_to_insert:
            try:
                result = col.insert_many(docs_to_insert, ordered=False)
                print(f"Inserted {len(result.inserted_ids)} events "
                    f"({sum(1 for d in docs_to_insert if d['doc_type']=='success')} success, "
                    f"{sum(1 for d in docs_to_insert if d['doc_type']=='failed')} failed)")
            except BulkWriteError as bwe:
                # Log a concise summary; you can inspect bwe.details for more
                print(f"Bulk write error: {len(bwe.details.get('writeErrors', []))} failed writes")  
        else:
            print("Nothing to insert")    

        print("Sleeping..!!")
        # Sleep for 60 seconds before next poll
        time.sleep(60)

