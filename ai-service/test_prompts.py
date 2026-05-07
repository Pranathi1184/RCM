from services.groq_client import GroqClient

client = GroqClient()

test_inputs = [
    "New RBI compliance rules for banks",
    "Financial penalties imposed by SEBI",
    "Operational delays due to internal processes",
    "Legal case regarding corporate governance",
    "Risk assessment for new investment",
    "Compliance audit requirements",
    "Internal operational policy update",
    "Financial reporting standards change",
    "Legal obligations for startups",
    "Risk mitigation strategies"
]

def test_categorise_prompt():
    print("\n--- Testing Categorise Prompt ---\n")

    for text in test_inputs:
        prompt = f"""
Classify the text into one category:
Compliance, Risk, Legal, Financial, Operational

Text: "{text}"

Return JSON:
{{
  "category": "...",
  "confidence": 0.0,
  "reasoning": "..."
}}
"""

        response = client.generate(prompt)

        print(f"\nInput: {text}")
        print("Response:", response)


if __name__ == "__main__":
    test_categorise_prompt()