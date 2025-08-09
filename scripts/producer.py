import json
import random
import time
from datetime import datetime, timezone
from kafka import KafkaProducer
from constants import KAFKA_BOOTSTRAP_SERVERS, TOPIC_NAME_USER_EVENTS, EVENT_TYPES, PAGE_URLS, UserEvent
from utils import generate_kafka_producer, create_topic_if_not_exists



def generate_event() -> UserEvent:
    event_type = random.choice(EVENT_TYPES)
    event: UserEvent = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "user_id": f"usr_{random.randint(0, 10)}",
        "event_type": event_type,
        "page_url": PAGE_URLS[event_type],
        "session_id": f"sess_{random.randint(50, 100)}"
    }
    return event

def send_event(producer, topic_name):
    event = generate_event()
    event_json = json.dumps(event)
    producer.send(topic_name, value=event_json)
    print(f"📤 Sent event: {event_json}")

if __name__ == '__main__':
    create_topic_if_not_exists(KAFKA_BOOTSTRAP_SERVERS, TOPIC_NAME_USER_EVENTS)

    producer = generate_kafka_producer(KAFKA_BOOTSTRAP_SERVERS)

    for _ in range(10):  # Send 10 events
        send_event(producer, TOPIC_NAME_USER_EVENTS)
        time.sleep(1)  # Wait 1 second between messages

    producer.flush()
    producer.close()


