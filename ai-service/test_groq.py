import os
import requests
from dotenv import load_dotenv

load_dotenv()

API_KEY = os.getenv("GROQ_API_KEY")

if not API_KEY:
    raise ValueError("GROQ_API_KEY not set")

URL = "https://api.groq.com/openai/v1/chat/completions"

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

payload = {
    "model": "llama-3.3-70b-versatile",
    "messages": [
        {"role": "user", "content": "Explain Artificial Intelligence in one sentence"}
    ],
    "temperature": 0.5
}

try:
    response = requests.post(URL, headers=headers, json=payload, timeout=10)
    response.raise_for_status()

    data = response.json()

    print("SUCCESS RESPONSE:")
    print(data["choices"][0]["message"]["content"])

except Exception as e:
    print("ERROR:", str(e))