try:
    __import__('pysqlite3')
    import sys
    sys.modules['sqlite3'] = sys.modules.pop('pysqlite3')
except ImportError:
    pass

import chromadb
from chromadb.utils import embedding_functions
import os

class ChromaService:
    def __init__(self):
        self.persist_directory = os.path.join(os.getcwd(), "chroma_data")

        self.client = chromadb.PersistentClient(path=self.persist_directory)

        self.embedding_fn = embedding_functions.SentenceTransformerEmbeddingFunction(
            model_name="all-MiniLM-L6-v2"
        )

        self.collection_name = "regulatory_docs"
        self.collection = self.client.get_or_create_collection(
            name=self.collection_name,
            embedding_function=self.embedding_fn
        )

    def add_documents(self, documents, metadatas, ids):
        """Add documents safely (avoid duplicate ID crash)."""
        try:
            existing = self.collection.get()
            existing_ids = set(existing.get("ids", []))
        except Exception:
            existing_ids = set()

        new_docs, new_meta, new_ids = [], [], []

        for doc, meta, id_ in zip(documents, metadatas, ids):
            if id_ not in existing_ids:
                new_docs.append(doc)
                new_meta.append(meta)
                new_ids.append(id_)

        if new_docs:
            self.collection.add(
                documents=new_docs,
                metadatas=new_meta,
                ids=new_ids
            )

    def query(self, query_text, n_results=3):
        return self.collection.query(
            query_texts=[query_text],
            n_results=n_results
        )


if __name__ == "__main__":
    service = ChromaService()
    print(f"ChromaDB initialized. Collection '{service.collection_name}' ready.")