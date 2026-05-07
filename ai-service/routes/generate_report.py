from flask import Blueprint, request, jsonify
from services.groq_client import GroqClient
import threading
import uuid
import time
import re
import json
import os

def load_prompt(filename, **kwargs):
    path = os.path.join("ai-service", "prompts", filename)
    with open(path, "r") as f:
        template = f.read()
    return template.format(**kwargs)

fallback_report = {
    "title": "Report Unavailable",
    "executive_summary": "AI service is currently unavailable.",
    "overview": "Please try again later.",
    "top_items": [],
    "recommendations": []
}

generate_report_bp = Blueprint("generate_report", __name__)
groq = GroqClient()

# In-memory job store
jobs = {}

def clean_ai_json(raw_text):
    """Remove markdown and safely parse JSON."""
    cleaned = raw_text.replace("```json", "").replace("```", "").strip()
    match = re.search(r"\{.*\}", cleaned, re.DOTALL)

    if match:
        try:
            return json.loads(match.group())
        except:
            return fallback_report  # fallback if parsing fails
    return fallback_report


def process_report(job_id, text):
    try:
        prompt = load_prompt("generate_report.txt", text=text)

        result = groq.generate(prompt)

        raw_output = result["content"]

        # CLEAN RESPONSE
        parsed_output = clean_ai_json(raw_output)

        # UPDATE JOB
        jobs[job_id]["status"] = "completed"
        jobs[job_id]["result"] = parsed_output
        jobs[job_id]["is_fallback"] = result.get("is_fallback", False) or (parsed_output == fallback_report)
        jobs[job_id]["meta"] = {
            "model_used": result.get("model", "unknown"),
            "tokens_used": result.get("tokens", 0)
        }

    except Exception:
        jobs[job_id]["status"] = "completed"
        jobs[job_id]["result"] = fallback_report
        jobs[job_id]["is_fallback"] = True


@generate_report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.get_json()

    if not data or "text" not in data:
        return jsonify({"error": "Missing 'text' field"}), 400

    text = data["text"]

    # Optional input guard (good practice)
    if not text.strip():
        return jsonify({"error": "Text cannot be empty"}), 400

    # Create job
    job_id = str(uuid.uuid4())

    jobs[job_id] = {
        "status": "processing",
        "result": None,
        "is_fallback": False
    }

    # Start background thread
    thread = threading.Thread(target=process_report, args=(job_id, text))
    thread.start()

    return jsonify({"job_id": job_id})


@generate_report_bp.route("/report-status/<job_id>", methods=["GET"])
def get_status(job_id):
    if job_id not in jobs:
        return jsonify({"error": "Invalid job_id"}), 404

    return jsonify({
        "status": jobs[job_id]["status"],
        "result": jobs[job_id]["result"],
        "is_fallback": jobs[job_id]["is_fallback"]
    })