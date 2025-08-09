from kafka import KafkaProducer
from kafka.admin import KafkaAdminClient, NewTopic
from kafka.errors import TopicAlreadyExistsError
import json
from constants import UserEvent
from kafka import KafkaConsumer
import redis
from pymongo import MongoClient, ASCENDING
from pydantic import ValidationError




def get_kafka_consumer(kafka_boot_strap_server: str, topic_name: str, consumer_group_id: str):
    return KafkaConsumer(
        topic_name,
        bootstrap_servers=kafka_boot_strap_server,
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        group_id=consumer_group_id,
        value_deserializer=lambda x: x.decode('utf-8')
    )

def generate_kafka_producer(kafka_boot_strap_server: str) -> KafkaProducer:
    
    return KafkaProducer(
        bootstrap_servers=kafka_boot_strap_server,
        value_serializer=lambda v: v.encode('utf-8')
    )

def create_topic_if_not_exists(kafka_boot_strap_server: str, topic_name: str):
    admin_client = KafkaAdminClient(
        bootstrap_servers=kafka_boot_strap_server,
        client_id='topic_creator'
    )

    try:
        topic_list = [NewTopic(name=topic_name, num_partitions=1, replication_factor=1)]
        admin_client.create_topics(new_topics=topic_list, validate_only=False)
        print(f"✅ Topic '{topic_name}' created.")
    except TopicAlreadyExistsError:
        print(f"ℹ️ Topic '{topic_name}' already exists.")
    finally:
        admin_client.close()


def validate_events(event_str: str) -> UserEvent:
    try:
        user_event_dict = json.loads(event_str)
    except json.JSONDecodeError as e:
        raise ValueError(f"Invalid JSON: {e}")

    missing_fields = [f for f in UserEvent.__annotations__ if f not in user_event_dict]
    if missing_fields:
        raise ValueError(f"Missing fields: {missing_fields}")

    for field, expected_type in UserEvent.__annotations__.items():
        if not isinstance(user_event_dict[field], expected_type):
            raise ValueError(f"Field '{field}' should be {expected_type.__name__}")

    return user_event_dict 
    
    
def get_redis_client(redis_host: str, redis_port: int, redis_pwd: str) -> redis.Redis:
    return redis.Redis(
        host=redis_host,
        port=redis_port,
        password=redis_pwd,
        decode_responses=True  
    )

def get_mongo_client(mongo_uri: str) -> MongoClient:
    return MongoClient(mongo_uri)
