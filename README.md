# Regulatory Change Management Tool

A full-stack project for managing regulatory changes with AI-powered insights (summaries, recommendations) and RAG-based search.

## Features
- **Dashboard**: Real-time KPIs and AI-powered Regulatory Assistant (RAG).
- **Regulatory Changes**: Full CRUD with status tracking and priority management.
- **AI Integration**: Automatic summaries and recommendations using Groq LLM.
- **Audit Trail**: Automatic logging of all changes (Create, Update, Delete) with old/new value tracking.
- **Role-Based Access**: Secure endpoints for ADMIN, MANAGER, and VIEWERS.
- **Exporting**: Export regulatory data to CSV for reporting.
- **Notifications**: Automatic email alerts for upcoming deadlines and overdue items.

## Tech Stack
- **Frontend**: React 18, Vite, Tailwind CSS, Axios, React Router.
- **Backend**: Java 17, Spring Boot 3, PostgreSQL, Redis, Flyway, Spring Security (JWT).
- **AI Service**: Python Flask, Groq LLM, ChromaDB (Vector DB), Sentence Transformers.

## Architecture
```text
      +----------------+      +----------------+      +----------------+
      |   React SPA    | <--> | Spring Boot    | <--> |  PostgreSQL    |
      |   (Frontend)   |      |   (Backend)    |      |  (Main DB)     |
      +-------+--------+      +-------+--------+      +----------------+
              ^                       |
              |                       v
              |               +-------+--------+      +----------------+
              +-------------> | Flask AI       | <--> |  ChromaDB      |
                              | Service        |      |  (Vector DB)   |
                              +-------+--------+      +----------------+
                                      |
                                      v
                              +-------+--------+
                              |   Groq LLM     |
                              |   (Llama 3)    |
                              +----------------+
```

## Demo Scenarios
1. **Scenario 1: New Regulation**: Login as `manager@example.com`, create a new "GDPR Data Privacy" change, and see AI generate a summary and recommendations instantly.
2. **Scenario 2: Regulatory Assistant**: Ask "What are the latest SEC updates for 2026?" in the Dashboard search and see RAG-powered answers from the Tool-13 PDF.
3. **Scenario 3: Audit Compliance**: Change the status of a record from `DRAFT` to `APPROVED`, then view the Audit Log in the database to see the exact field changes.
4. **Scenario 4: Security Verification**: Try to access `/api/changes` without a token or with a `VIEWER` role trying to `DELETE` a record, and see the `401`/`403` responses.
5. **Scenario 5: Performance & Cache**: Observe fast dashboard loading thanks to Redis caching and optimized SQL queries.

## Project Structure
```
.
├── ai-service/       # Python AI microservice
├── backend/          # Spring Boot REST API
├── frontend/         # React SPA
├── docker-compose.yml
└── .env.example
```

## Quick Start
1. Clone the repository.
2. Setup environment variables:
   ```bash
   cp .env.example .env
   # Add your GROQ_API_KEY to .env
   ```
3. Run with Docker:
   ```bash
   docker-compose up --build
   ```
4. Access the apps:
   - Frontend: `http://localhost`
   - Backend API: `http://localhost:8080`
   - AI Service: `http://localhost:5000`

## Demo Credentials
- **Admin**: `admin@example.com` / `admin123`
- **Manager**: `manager@example.com` / `admin123`
- **Viewer**: `viewer@example.com` / `admin123`

## Development
- **Ingest Documents**: Run `python ai-service/ingest_docs.py` to index the regulatory PDF into ChromaDB.
- **Backend Tests**: Run `./mvnw test` in the `backend` directory.
- **Frontend Dev**: Run `npm run dev` in the `frontend` directory.
