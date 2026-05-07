# test_client.py

from services.groq_client import GroqClient

client = GroqClient()

response = client.generate("What is machine learning?")
print(response)