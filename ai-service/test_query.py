import requests

URL = "http://127.0.0.1:5000/query"

test_inputs = [
    "What is compliance?",
    "How do compliance officers monitor updates?",
    "What is risk management?"
]

def test_query():
    print("\n--- Testing /query ---\n")

    for q in test_inputs:
        response = requests.post(URL, json={"question": q})
        data = response.json()

        print(f"\nQuestion: {q}")
        print("RAW:", data)

        # SAFE HANDLING
        if "data" in data:
            print("Answer:", data["data"]["answer"][:100], "...")
        else:
            print("Error:", data.get("error", "Unknown issue"))

if __name__ == "__main__":
    test_query()