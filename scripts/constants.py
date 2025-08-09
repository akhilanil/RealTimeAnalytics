from typing import TypedDict



KAFKA_BOOTSTRAP_SERVERS = 'localhost:9094'
TOPIC_NAME_USER_EVENTS = 'user-events'

EVENT_TYPES = [
    "page_view",
    "payment_done",
    "add_cart",
    "order_return",
    "remove_cart"
]

PAGE_URLS = {
    "page_view": "/products/electronics",
    "payment_done": "/checkout/success",
    "add_cart": "/products/add",
    "order_return": "/orders/return",
    "remove_cart": "/cart/remove"
}

CONSUMER_GROUP_ID_PROCESSOR = 'event-processor'
CONSUMER_GROUP_ID_AUDIT = 'audit'

class UserEvent(TypedDict):
    timestamp: str
    user_id: str
    event_type:str
    page_url: str
    session_id: str
