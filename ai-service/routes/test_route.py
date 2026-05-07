from flask import Blueprint, request, jsonify
from services.limiter import limiter
from services.ai_service import process_text
from services.auth_service import generate_token, verify_token
from services.logger import logger

test_bp = Blueprint("test", __name__)


@test_bp.route("/login", methods=["POST"])
def login():
    if not request.is_json:
        return jsonify({
            "success": False,
            "error": "Request must be in JSON format"
        }), 400

    username = request.json.get("username")
    password = request.json.get("password")

    if username == "admin" and password == "password":
        token = generate_token(username)
        return jsonify({
            "success": True,
            "token": token
        })

    return jsonify({
        "success": False,
        "error": "Invalid credentials"
    }), 401


@test_bp.route("/test", methods=["POST"])
@limiter.limit("5 per minute")
def test():
    auth_header = request.headers.get("Authorization")

    if not auth_header:
        return jsonify({
            "success": False,
            "error": "Authorization header missing"
        }), 401

    try:
        token = auth_header.split(" ")[1]
    except:
        return jsonify({
            "success": False,
            "error": "Invalid token format"
        }), 401

    decoded = verify_token(token)

    if not decoded:
        return jsonify({
            "success": False,
            "error": "Invalid or expired token"
        }), 401

    if not request.is_json:
        logger.warning("Request received without JSON format")
        return jsonify({
            "success": False,
            "error": "Request must be in JSON format"
        }), 400

    data = request.json.get("text", "")

    if not data:
        return jsonify({
            "success": False,
            "error": "Text field is required"
        }), 400

    if "drop" in data.lower():
        return jsonify({
            "success": False,
            "error": "Invalid or malicious input detected",
            "field": "text"
        }), 400

    logger.info(f"Received input: {data}")

    result = process_text(data)

    logger.info(f"Processed result: {result}")

    return jsonify({
        "success": True,
        "data": {
            "input": data,
            "response": result
        }
    })