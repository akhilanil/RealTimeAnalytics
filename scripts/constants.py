from typing import TypedDict



KAFKA_BOOTSTRAP_SERVERS = 'localhost:9094'
TOPIC_NAME_USER_EVENTS = 'user-events'


PAGE_URLS = [
    "/products/electronics",
    "/checkout/success",
    "/products/add",
    "/orders/return",
    "/purchase/",
    "/cart/add",
    "/cart/remove",
    "/product/remove",
    "/products/gloves",
    "/products/bottles"
]

CONSUMER_GROUP_ID_PROCESSOR = 'event-processor'
CONSUMER_GROUP_ID_AUDIT = 'audit'

class UserEvent(TypedDict):
    timestamp: str
    user_id: str
    event_type:str
    page_url: str
    session_id: str
