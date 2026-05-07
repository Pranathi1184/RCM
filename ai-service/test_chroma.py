from services.chroma_service import ChromaService

def test_chroma():
    print("Testing ChromaDB Setup...")

    service = ChromaService()

    test_docs = [
    "Compliance is the process of adhering to laws, regulations, and organizational policies.",
    "Regulatory change management is the process of tracking and implementing changes in laws and regulations.",
    "Compliance officers use tools to automate the monitoring of regulatory updates.",
    "Risk management involves identifying and mitigating potential threats to an organization."
]

    test_metadatas = [
        {"source": "doc1", "topic": "rcm"},
        {"source": "doc2", "topic": "compliance"},
        {"source": "doc3", "topic": "risk"}
    ]

    test_ids = ["id1", "id2", "id3"]

    print("Adding test documents...")
    service.add_documents(test_docs, test_metadatas, test_ids)

    query = "How do compliance officers monitor updates?"
    print(f"Querying: '{query}'")

    results = service.query(query, n_results=1)

    docs = results.get("documents", [[]])[0]

    if docs:
        matched_doc = docs[0]
        print(f"Matched Document: {matched_doc}")

        if "Compliance officers use tools" in matched_doc:
            print("SUCCESS: Correct document retrieved!")
        else:
            print("WARNING: Retrieved but may not be exact match")
    else:
        print("FAILURE: No results returned.")


if __name__ == "__main__":
    test_chroma()