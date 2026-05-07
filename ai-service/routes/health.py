from flask import Blueprint, jsonify
import time
from services.chroma_service import ChromaService
from services.cache_service import get_cache_stats

health_bp = Blueprint("health", __name__)

start_time = time.time()

# store last 10 response times
response_times = []

chroma = ChromaService()

MODEL_NAME = "llama-3.3-70b-versatile"

@health_bp.route("/health", methods=["GET"])
def health():
    uptime = time.time() - start_time

    # avg response time
    avg_time = sum(response_times) / len(response_times) if response_times else 0

    # chroma doc count
    try:
        data = chroma.collection.get()
        doc_count = len(data.get("ids", []))
    except:
        doc_count = 0

    return jsonify({
        "model": MODEL_NAME,
        "avg_response_time": round(avg_time, 3),
        "chroma_doc_count": doc_count,
        "uptime_seconds": int(uptime),
        "cache": get_cache_stats() 
    })