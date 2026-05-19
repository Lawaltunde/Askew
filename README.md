# Askew

A full-stack web application that generates 3 role-specific interview questions for a given job title using OpenAI via Spring AI.

**Live Demo:** [askew-chgtojx5h-lawaltundes-projects.vercel.app](https://askew-chgtojx5h-lawaltundes-projects.vercel.app)

---

## What This Does

Enter a job title (e.g. "Customer Success Manager"), click Generate, and the app returns 3 thoughtful, role-specific interview questions powered by GPT-4o mini. Every generation is persisted to Postgres and viewable on the History page.

---

## Tech Stack

| Layer | Choice |
|---|---|
| Backend | Spring Boot 3.4.5 (Java 17) |
| AI Integration | Spring AI 1.0.0 + OpenAI `gpt-4o-mini` |
| Database | Supabase Postgres (connection pooler) |
| Frontend | Vite 8 + React 18 + React Router |
| Backend Host | Render (Docker) |
| Frontend Host | Vercel |

---

## Architecture

```
┌─────────────────┐         ┌─────────────────────────┐
│  React (Vercel) │ ──POST─▶│  Spring Boot (Render)   │
│  2-page SPA     │ ──GET──▶│  /api/v1/interviews     │
└─────────────────┘         └───────────┬─────────────┘
                                        │
                       ┌────────────────┼────────────────┐
                       ▼                                 ▼
            ┌─────────────────────┐       ┌─────────────────────────┐
            │  Spring AI          │       │  Spring Data JPA        │
            │  ChatClient         │       │  → Supabase Postgres    │
            └──────────┬──────────┘       └─────────────────────────┘
                       ▼
            ┌─────────────────────┐
            │  OpenAI gpt-4o-mini │
            └─────────────────────┘
```

### Backend Layers

- **Controller** — HTTP boundary, validates request, returns response
- **InterviewService / AiService** — interfaces; impls are `InterviewServiceImpl` / `AiServiceImpl`
- **AiServiceImpl** — thin wrapper around Spring AI `ChatClient`
- **DTOs** — `InterviewRequest`, `InterviewResponse`, `InterviewHistoryItem`
- **Entity + Repository** — JPA persistence with `findAllByOrderByCreatedAtDesc()`

---

## Project Structure

```
Askew/
├── src/main/java/com/askew/
│   ├── AskewApplication.java
│   ├── config/CorsConfig.java
│   ├── controller/InterviewController.java
│   ├── dto/
│   │   ├── InterviewRequest.java
│   │   ├── InterviewResponse.java
│   │   └── InterviewHistoryItem.java
│   ├── entity/Interview.java
│   ├── repository/InterviewRepository.java
│   └── service/
│       ├── AiService.java          ← interface
│       ├── AiServiceImpl.java
│       ├── InterviewService.java   ← interface
│       └── InterviewServiceImpl.java
├── src/main/resources/application.yml
├── src/test/                       ← 22 unit tests
├── frontend/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── api.js
│   │   ├── components/Nav.jsx
│   │   └── pages/
│   │       ├── Home.jsx            ← Generate page
│   │       └── History.jsx         ← Past interviews
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── Dockerfile                      ← backend
├── docker-compose.yml              ← full local stack
└── pom.xml
```

---

## API

### `POST /api/v1/interviews/generate`

**Request**
```json
{ "jobTitle": "Customer Success Manager" }
```

**Response (200)**
```json
{
  "jobTitle": "Customer Success Manager",
  "questions": [
    "How would you identify and address churn risk in your first 90 days?",
    "Walk me through how you'd structure an onboarding program for a new enterprise client.",
    "Describe a time you turned around a struggling customer relationship."
  ]
}
```

**Errors:** `400` blank/missing jobTitle · `500` unexpected error

---

### `GET /api/v1/interviews`

Returns all past generations ordered by most recent.

**Response (200)**
```json
[
  {
    "id": 1,
    "jobTitle": "Customer Success Manager",
    "questions": ["...", "...", "..."],
    "createdAt": "2026-05-19T10:00:00"
  }
]
```

---

## Local Development

### Prerequisites

- Java 17+
- Node 20+
- OpenAI API key
- Supabase project — use the **connection pooler** JDBC URL (port 6543)

### Option A — Run natively

```bash
# Backend (from repo root)
source .env          # see .env setup below
./mvnw spring-boot:run

# Frontend (new terminal)
cd frontend
npm install
npm run dev          # Vite proxies /api/* to localhost:8080 automatically
```

### Option B — Run with Docker Compose

```bash
export OPENAI_API_KEY=sk-...
docker compose up --build
```

Runs backend on `:8080`, frontend on `:5173`, Postgres on `:5432`.

### `.env` file (Option A)

```bash
export OPENAI_API_KEY=sk-...
export DATABASE_URL=jdbc:postgresql://<pooler-host>:6543/postgres
export DATABASE_USERNAME=postgres.<project-ref>
export DATABASE_PASSWORD=your_supabase_password
export FRONTEND_URL=http://localhost:5174
```

---

## Deployment

### Backend → Render

1. Create a **Web Service** on Render, connect this repo
2. Runtime: **Docker** (Dockerfile is at repo root)
3. Set environment variables:
   - `OPENAI_API_KEY`
   - `DATABASE_URL` — Supabase pooler JDBC URL (port 6543)
   - `DATABASE_USERNAME` — `postgres.<project-ref>`
   - `DATABASE_PASSWORD`
   - `FRONTEND_URL` — your Vercel URL (set after frontend deploy)

### Frontend → Vercel

1. Import this repo on Vercel
2. Set **Root Directory** to `frontend`
3. Add environment variable:
   - `VITE_API_URL` — your Render URL (e.g. `https://askew.onrender.com`)
4. Deploy, then copy the Vercel URL back into Render's `FRONTEND_URL`

---

## ⚠️ Free-Tier Cold Starts

- **Render** spins down after 15 min inactivity — first request takes 30–60s
- **Supabase** pauses after 7 days inactivity — resume is a one-click action in the dashboard

Hit the live URL once before sharing to wake everything up.

---

## Security

- OpenAI API key and DB credentials live only on the backend as environment variables
- CORS restricted to the deployed frontend origin via `allowedOriginPatterns`
- Input capped at 200 characters server-side to prevent prompt injection

---

## License

MIT