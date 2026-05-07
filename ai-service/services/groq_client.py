import os
import requests
import time
import logging
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(level=logging.INFO)

class GroqClient:
    def __init__(self):
        self.api_key = os.getenv("GROQ_API_KEY")
        if not self.api_key:
            raise ValueError("GROQ_API_KEY not found in .env")

        self.url = "https://api.groq.com/openai/v1/chat/completions"

        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }

    def generate(self, prompt, retries=3):
        payload = {
            "model": "llama-3.3-70b-versatile",
            "messages": [
                {"role": "user", "content": prompt}
            ],
            "temperature": 0.5
        }

        for attempt in range(retries):
            try:
                response = requests.post(
                    self.url,
                    headers=self.headers,
                    json=payload,
                    timeout=10
                )

                response.raise_for_status()
                data = response.json()

                if "choices" in data and len(data["choices"]) > 0:
                    return {
                        "content": data["choices"][0]["message"]["content"],
                        "tokens": data.get("usage", {}).get("total_tokens", 0),
                        "model": data.get("model", "llama-3.3-70b-versatile"),
                        "is_fallback": False
                    }

                logging.warning("Unexpected response format")
                return {
                    "content": "Our AI is currently refining its thoughts. Please try again in a moment.",
                    "tokens": 0,
                    "model": "unknown",
                    "is_fallback": True
                }

            except requests.exceptions.Timeout:
                logging.error(f"Timeout (attempt {attempt+1})")

            except requests.exceptions.RequestException as e:
                logging.error(f"Request failed: {e}")

            time.sleep(2 ** attempt)

        return {
            "content": "The regulatory assistant is taking a short break. Here is a standard template for now: This change requires careful review of compliance standards.",
            "tokens": 0,
            "model": "unknown",
            "is_fallback": True
        }