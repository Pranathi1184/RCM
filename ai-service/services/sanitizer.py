import re
from flask import request, jsonify

def is_malicious(input_text):
    if not isinstance(input_text, str):
        return False

    patterns = [
        r"<script.*?>.*?</script>",
        r"DROP TABLE",
        r"SELECT .* FROM",
        r"INSERT INTO",
        r"DELETE FROM",
        r"--",
        r";",
        r"ignore previous instructions",
        r"system prompt",
        r"act as",
    ]

    for pattern in patterns:
        if re.search(pattern, input_text, re.IGNORECASE):
            return True

    return False


def sanitize_request():
    if request.method in ["POST", "PUT", "PATCH"]:
        data = request.get_json(silent=True)

        if not data:
            return

        for key, value in data.items():
            if isinstance(value, str) and is_malicious(value):
                return jsonify({
                    "error": "Invalid or malicious input detected",
                    "field": key
                }), 400