import time
from constants import TOPIC_NAME_USER_EVENTS, CONSUMER_GROUP_ID_PROCESSOR, KAFKA_BOOTSTRAP_SERVERS, UserEvent
from utils import validate_events, get_kafka_consumer, get_redis_client
import os
from redis import Redis
from datetime import datetime
from pymongo import MongoClient, ASCENDING



def insert_to_redis(redis_client: Redis, user_event:UserEvent):
    timestamp = datetime.fromisoformat(user_event["timestamp"])
    minute_bucket = timestamp.strftime("%Y%m%d%H%M")  # e.g., 202508071634

    user_id = user_event["user_id"]
    page_url = user_event["page_url"]
    session_id = user_event["session_id"]

     # Active Users
    redis_client.sadd(f"active_users:{minute_bucket}", user_id)
    redis_client.expire(f"active_users:{minute_bucket}", 300)

    # Page Views by URL
    redis_client.zincrby(f"page_views:{minute_bucket}", 1.0, page_url)
    redis_client.expire(f"page_views:{minute_bucket}", 900)

    # Active Sessions per User
    redis_client.sadd(f"user_sessions:{user_id}:{minute_bucket}", session_id)
    redis_client.expire(f"user_sessions:{user_id}:{minute_bucket}", 300)


if __name__ == '__main__':

    print("Waiting for kafa to initialise")
    time.sleep(10)

    kafka_bootstrap_server = os.getenv('KAFKA_BOOTSTRAP_SERVERS', KAFKA_BOOTSTRAP_SERVERS)
    consumer_group_id = os.getenv('CONSUMER_GROUP_ID', CONSUMER_GROUP_ID_PROCESSOR)


    print(f"Connecting to kafka server: {kafka_bootstrap_server} to topic {TOPIC_NAME_USER_EVENTS} consumer group: {consumer_group_id}", )
    consumer = get_kafka_consumer(kafka_bootstrap_server, TOPIC_NAME_USER_EVENTS, consumer_group_id)

    print(f"Initilising redis", )
    redis_client: Redis = get_redis_client(
        redis_host=os.environ['REDIS_HOST'],
        redis_port=int(os.environ['REDIS_PORT']),
        redis_pwd=os.environ['REDIS_PASSWORD']                             
    )
    

    while True:
        print("Polling for new message")
        # Poll messages for a short duration (non-blocking)
        # Reads only 100 messages per second
        messages = consumer.poll(timeout_ms=1000, max_records=100)

        for tp, records in messages.items():
            for record in records:
                try:
                    user_event = validate_events(record.value)
                    insert_to_redis(redis_client=redis_client, user_event=user_event)
                    print(record.value)
                except ValueError:
                    # Just log these.
                    print("Invalid event: ", record.value)
                

        # Sleep for 10 seconds before next poll
        time.sleep(5)

