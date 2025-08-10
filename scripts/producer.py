import json
import random
import time
from datetime import datetime, timezone
from kafka import KafkaProducer
from constants import KAFKA_BOOTSTRAP_SERVERS, TOPIC_NAME_USER_EVENTS, PAGE_URLS, UserEvent
from utils import generate_kafka_producer, create_topic_if_not_exists
import signal
import os


def generate_event() -> UserEvent:
    event_type = 'page_view'
    event: UserEvent = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "user_id": f"usr_{random.randint(0, 30)}",
        "event_type": event_type,
        "page_url": random.choice(PAGE_URLS),
        "session_id": f"sess_{random.randint(50, 100)}"
    }
    return event

def send_event(producer, topic_name):
    event = generate_event()
    event_json = json.dumps(event)
    producer.send(topic_name, value=event_json)
    print(f"📤 Sent event: {event_json}")


def main():
    print("Waiting for kafa to initialise")
    time.sleep(20)

    kafka_bootstrap_server = os.getenv('KAFKA_BOOTSTRAP_SERVERS', KAFKA_BOOTSTRAP_SERVERS)
    wait_for_next_batch = int(os.getenv('WAIT_SECONDS_AFTER_BATCH', 5))

    create_topic_if_not_exists(kafka_bootstrap_server, TOPIC_NAME_USER_EVENTS)
    print(f"Connecting to kafka server: {kafka_bootstrap_server} to topic {TOPIC_NAME_USER_EVENTS} to push events", )
    producer = generate_kafka_producer(kafka_bootstrap_server)

    stop = False
    def _stop(*_):
        nonlocal stop
        stop = True
    signal.signal(signal.SIGINT, _stop)
    signal.signal(signal.SIGTERM, _stop)

    print("Starting to emit events")
    try:
        while not stop:
            total_events_per_second = random.randint(20, 100)
            interval = 1.0 / total_events_per_second

            print(f"Publishing {total_events_per_second} events...")
            batch_start = time.perf_counter()

            # Send events evenly spaced
            for _ in range(total_events_per_second):
                send_event(producer, TOPIC_NAME_USER_EVENTS)
                time.sleep(interval)

            producer.flush(timeout=0.5)
            elapsed = time.perf_counter() - batch_start
            print(f"Batch done in {elapsed:.2f}s. Waiting {wait_for_next_batch} seconds...")
            
            time.sleep(wait_for_next_batch)  
    finally:
        producer.flush()
        producer.close()

if __name__ == '__main__':
    main()

