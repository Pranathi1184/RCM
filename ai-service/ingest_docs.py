
import pypdf
import os
import sys

# Add the project root to sys.path to import ChromaService
sys.path.append(os.path.join(os.getcwd(), "ai-service"))

from services.chroma_service import ChromaService

def chunk_text(text, chunk_size=500, overlap=50):
    chunks = []
    for i in range(0, len(text), chunk_size - overlap):
        chunks.append(text[i:i + chunk_size])
    return chunks

def ingest_file(file_path):
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return

    print(f"Ingesting: {file_path}")
    
    full_text = ""
    if file_path.endswith(".pdf"):
        with open(file_path, "rb") as f:
            reader = pypdf.PdfReader(f)
            for page in reader.pages:
                full_text += page.extract_text() + "\n"
    else:
        with open(file_path, "r", encoding="utf-8") as f:
            full_text = f.read()
    
    chunks = chunk_text(full_text)
    
    chroma = ChromaService()
    
    documents = chunks
    metadatas = [{"source": os.path.basename(file_path)} for _ in chunks]
    ids = [f"{os.path.basename(file_path)}_{i}" for i in range(len(chunks))]
    
    print(f"Adding {len(chunks)} chunks to ChromaDB...")
    chroma.add_documents(documents, metadatas, ids)
    print("Ingestion complete.")

if __name__ == "__main__":
    # Ingest the new Knowledge Base
    kb_to_ingest = r"d:\pranathi\projects\campus pe\regulatory-change-management\regulatory_kb.txt"
    ingest_file(kb_to_ingest)
