import requests

URL = "http://127.0.0.1:5000/categorise"

test_inputs = [
    "RBI compliance update for banks",
    "SEBI penalty regulations",
    "Internal operational delay policy",
    "Legal dispute over contracts",
    "Financial audit reporting standards",
    "Risk mitigation strategy document",
    "Corporate governance legal rules",
    "Compliance audit checklist",
    "Operational workflow improvement",
    "Investment risk analysis"
]

def test_categorise():
    print("\n--- Testing /categorise ---\n")

    total_score = 0

    for text in test_inputs:
        response = requests.post(URL, json={"text": text})
        data = response.json()

        print(f"\nInput: {text}")
        print("Category:", data["data"]["category"])
        print("Confidence:", data["meta"]["confidence"])

        score = int(input("Score (1-5): "))
        total_score += score

    avg = total_score / len(test_inputs)

    print("\n--- RESULT ---")
    print(f"Average Score: {avg:.2f}")

if __name__ == "__main__":
    test_categorise()