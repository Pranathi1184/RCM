
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
import os
import time
from services.cache_service import get_cache, set_cache

describe_bp = Blueprint("describe", __name__)
client = GroqClient()

def load_prompt(filename, **kwargs):
    path = os.path.join("ai-service", "prompts", filename)
    with open(path, "r") as f:
        template = f.read()
    return template.format(**kwargs)

@describe_bp.route("/describe", methods=["POST"])
def describe():
    data = request.get_json()
    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' field"}), 400

    user_text = data["text"]

    # CACHE CHECK
    key = "desc:" + user_text.lower().strip()
    cached = get_cache(key)
    if cached:
        return jsonify({
            "data": cached,
            "meta": {
                "confidence": 1.0,
                "model_used": "cache",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": True,
                "is_fallback": False
            }
        })

    try:
        prompt = load_prompt("describe.txt", text=user_text)
        
        start = time.time()
        response_data = client.generate(prompt)
        end = time.time()
        
        response_time_ms = int((end - start) * 1000)
        
        result = {
            "description": response_data["content"]
        }

        # SAVE TO CACHE
        set_cache(key, result)

        return jsonify({
            "data": result,
            "meta": {
                "confidence": 0.95,
                "model_used": response_data.get("model", "unknown"),
                "tokens_used": response_data.get("tokens", 0),
                "response_time_ms": response_time_ms,
                "cached": False,
                "is_fallback": response_data.get("is_fallback", False)
            }
        })
    except Exception as e:
        return jsonify({
            "data": {"description": "Unable to generate description at this time."},
            "meta": {
                "confidence": 0.0,
                "model_used": "fallback",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": False,
                "is_fallback": True
            }
        })
