from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
import json
import re
import time
from services.cache_service import get_cache, set_cache

import os

def load_prompt(filename, **kwargs):
    prompts_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "prompts"))
    path = os.path.join(prompts_dir, filename)
    with open(path, "r") as f:
        template = f.read()
    return template.format(**kwargs)

fallback_categorise = {
    "category": "Operational",
    "confidence": 0.5,
    "reasoning": "Fallback response due to AI service unavailability"
}

categorise_bp = Blueprint("categorise", __name__)

client = GroqClient()

@categorise_bp.route("/categorise", methods=["POST"])
def categorise():
    data = request.get_json()

    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' field"}), 400

    user_text = data["text"]
    
    if not user_text.strip():
        return jsonify({"error": "Text cannot be empty"}), 400

    if len(user_text) < 5:
        return jsonify({"error": "Text too short"}), 400

    if len(user_text) > 2000:
        return jsonify({"error": "Text too long"}), 400

    # CACHE CHECK
    key = user_text.lower().strip()
    cached = get_cache(key)
    if cached:
        return jsonify({
            "data": cached,
            "meta": {
                "confidence": cached.get("confidence", 1.0),
                "model_used": "cache",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": True,
                "is_fallback": False
            }
        })
    
    try:
        prompt = load_prompt("categorise.txt", text=user_text)
        start = time.time()
        ai_result = client.generate(prompt)
        end = time.time()
        response_time_ms = int((end - start) * 1000)

        raw_text = ai_result["content"]
        tokens_used = ai_result["tokens"]
        model_used = ai_result["model"]

        # Clean markdown
        cleaned = raw_text.replace("```json", "").replace("```", "").strip()
        match = re.search(r"\{.*\}", cleaned, re.DOTALL)

        if not match:
            raise Exception("Invalid JSON from AI")

        parsed = json.loads(match.group())
        
        # SAVE TO CACHE
        set_cache(key, parsed)

        return jsonify({
            "data": parsed,
            "meta": {
                "confidence": parsed.get("confidence", 0.9),
                "model_used": model_used,
                "tokens_used": tokens_used,
                "response_time_ms": response_time_ms,
                "cached": False,
                "is_fallback": False
            }
        })

    except Exception:
        return jsonify({
            "data": fallback_categorise,
            "meta": {
                "confidence": 0.5,
                "model_used": "fallback",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": False,
                "is_fallback": True
            }
        })
