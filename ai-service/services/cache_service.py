import redis
import hashlib
import json

# connect to Redis
r = redis.Redis(host='localhost', port=6379, decode_responses=True)

# stats
cache_hits = 0
cache_misses = 0

TTL = 900  # 15 minutes

def generate_key(text):
    return hashlib.sha256(text.encode()).hexdigest()

def get_cache(question):
    global cache_hits, cache_misses

    try:
        key = generate_key(question)
        data = r.get(key)

        if data:
            cache_hits += 1
            return json.loads(data)
    except Exception as e:
        print(f"Redis get error: {e}")

    cache_misses += 1
    return None

def set_cache(question, response):
    try:
        key = generate_key(question)
        r.setex(key, TTL, json.dumps(response))
    except Exception as e:
        print(f"Redis set error: {e}")

def get_cache_stats():
    return {
        "hits": cache_hits,
        "misses": cache_misses
    }