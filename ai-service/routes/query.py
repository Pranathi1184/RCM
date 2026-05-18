from flask import Blueprint, request, jsonify
from services.chroma_service import ChromaService
from services.groq_client import GroqClient
import time
from routes.health import response_times
from services.cache_service import get_cache, set_cache

import os

def load_prompt(filename, **kwargs):
    prompts_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "prompts"))
    path = os.path.join(prompts_dir, filename)
    with open(path, "r") as f:
        template = f.read()
    return template.format(**kwargs)

fallback_query = {
    "answer": "Unable to generate answer at the moment. Please try again later.",
    "sources": []
}

query_bp = Blueprint("query", __name__)

chroma = ChromaService()
groq = GroqClient()

@query_bp.route("/query", methods=["POST"])
def query():
    data = request.get_json()

    if not data or "question" not in data:
        return jsonify({"error": "Missing 'question' field"}), 400

    question = data["question"]
    
    if not question.strip():
        return jsonify({"error": "Question cannot be empty"}), 400

    if len(question) < 5:
        return jsonify({"error": "Question too short"}), 400

    if len(question) > 500:
        return jsonify({"error": "Question too long"}), 400

    try:
        # STEP 0 — CACHE CHECK
        key = question.lower().strip()
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

        # Step 1: Retrieve documents
        results = chroma.query(question, n_results=3)
        docs = results.get("documents", [[]])[0]
        metas = results.get("metadatas", [[]])[0]

        if not docs:
            raise Exception("No relevant documents")

        # Step 2: Build context
        context = "\n\n".join(docs)

        # Step 3: Prompt
        prompt = load_prompt("query.txt", context=context, question=question)

        # Step 4: Call Groq + track time
        start = time.time()
        ai_result = groq.generate(prompt)
        end = time.time()

        response_time_ms = int((end - start) * 1000)

        # Track avg time
        response_times.append(end - start)
        if len(response_times) > 10:
            response_times.pop(0)

        answer = ai_result["content"]
        tokens_used = ai_result["tokens"]
        model_used = ai_result["model"]

        # Format sources with their origin filenames
        sources = []
        for doc, meta in zip(docs, metas):
            sources.append({
                "content": doc,
                "file": meta.get("source", "Unknown")
            })

        # STEP 5 — SAVE TO CACHE
        result = {
            "answer": answer,
            "sources": sources
        }

        set_cache(key, result)

        return jsonify({
            "data": result,
            "meta": {
                "confidence": 0.9,
                "model_used": model_used,
                "tokens_used": tokens_used,
                "response_time_ms": response_time_ms,
                "cached": False,
                "is_fallback": ai_result.get("is_fallback", False)
            }
        })

    except Exception as e:
        return jsonify({
            "data": fallback_query,
            "meta": {
                "confidence": 0.5,
                "model_used": "fallback",
                "tokens_used": 0,
                "response_time_ms": 0,
                "cached": False,
                "is_fallback": True
            }
        })
