
from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
import os
import json
import re
import time
from services.cache_service import get_cache, set_cache

recommend_bp = Blueprint("recommend", __name__)
client = GroqClient()

def load_prompt(filename, **kwargs):
    path = os.path.join("ai-service", "prompts", filename)
    with open(path, "r") as f:
        template = f.read()
    return template.format(**kwargs)

@recommend_bp.route("/recommend", methods=["POST"])
def recommend():
    data = request.get_json()
    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' field"}), 400

    user_text = data["text"]

    # CACHE CHECK
    key = "rec:" + user_text.lower().strip()
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
        prompt = load_prompt("recommend.txt", text=user_text)
        
        start = time.time()
        response_data = client.generate(prompt)
        end = time.time()
        
        response_time_ms = int((end - start) * 1000)
        response_text = response_data["content"]
        
        # Extract JSON array
        match = re.search(r"\[.*\]", response_text, re.DOTALL)
        if not match:
            # Fallback for recommendation
            recommendations = [
                "Conduct a detailed review of internal compliance policies.",
                "Schedule a training session for the relevant departments.",
                "Monitor for further updates from the regulatory body."
            ]
        else:
            recommendations = json.loads(match.group())

        # SAVE TO CACHE
        set_cache(key, recommendations)

        return jsonify({
            "data": recommendations,
            "meta": {
                "confidence": 0.9,
                "model_used": response_data.get("model", "unknown"),
                "tokens_used": response_data.get("tokens", 0),
                "response_time_ms": response_time_ms,
                "cached": False,
                "is_fallback": response_data.get("is_fallback", not match)
            }
        })
        
    except Exception:
        return jsonify({
            "data": [
                "Review the regulation for specific compliance requirements.",
                "Assign a team lead to oversee implementation.",
                "Perform an initial impact assessment."
            ],
            "meta": {
                "confidence": 0.0,
                "model_used": "fallback",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": False,
                "is_fallback": True
            }
        })
