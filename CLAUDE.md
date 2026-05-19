# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Askew is a full-stack web application that generates 3 role-specific interview questions for a given job title using OpenAI via Spring AI. The backend persists each generated set of questions to Postgres.

**Current state:** The project is in early scaffolding stage. The `pom.xml` currently only has `spring-boot-starter-webmvc` — the dependencies below (Spring AI, JPA, Postgres, Lombok, Validation) still need to be added as the feature is built out.

## Commands

```bash
# Build
./mvnw clean package

# Run application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AskewApplicationTests

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

## Intended Architecture

```
React (Vite, Vercel) ──POST──▶ Spring Boot (Render) /api/v1/interviews/generate
                                        │
                        ┌───────────────┴───────────────┐
                        ▼                               ▼
               Spring AI ChatClient           Spring Data JPA
               → OpenAI gpt-4o-mini           → Supabase Postgres
```

### Backend Layers

- **Controller** — HTTP boundary, validates request, returns response
- **Service** — Orchestrates the AI call, parses the response, persists the result
- **AiService** — Thin wrapper around Spring AI's `ChatClient` (isolated for testability and prompt-tuning)
- **DTOs** — `InterviewRequest` (jobTitle) and `InterviewResponse` (jobTitle + `List<String>`)
- **Entity + Repository** — JPA persistence of generated interviews

No DAO wrapper or separate Mapper class — Spring Data JPA is the DAO, and inline mapping beats a dedicated class at this scale.

### API Contract

`POST /api/v1/interviews/generate`

Request: `{ "jobTitle": "Customer Success Manager" }`  
Response: `{ "jobTitle": "...", "questions": ["...", "...", "..."] }`  
Errors: `400` (blank jobTitle), `502` (OpenAI failure), `500` (unexpected)

### Key Design Decisions

- Questions stored as a `||`-delimited string in a single `TEXT` column (intentionally simple for demo; not ideal for querying)
- CORS restricted to the deployed frontend origin (not `*`) — configured in `CorsConfig`
- Input capped at 200 characters server-side to prevent prompt-injection abuse
- Spring AI BOM version: `1.1.2`; model: `gpt-4o-mini`; temperature: `0.7`

## Dependencies to Add

When implementing, the `pom.xml` needs:

```xml
<!-- Spring AI BOM in dependencyManagement -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bom</artifactId>
    <version>1.1.2</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<!-- Runtime dependencies -->
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-ai-openai-spring-boot-starter
postgresql (runtime)
lombok (optional)
```

## Environment Variables

| Variable | Description |
|---|---|
| `OPENAI_API_KEY` | OpenAI API key — backend only, never exposed to browser |
| `DATABASE_URL` | Supabase JDBC pooler URL (port 6543, not 5432) |
| `DATABASE_USERNAME` | Postgres username |
| `DATABASE_PASSWORD` | Postgres password |
| `FRONTEND_URL` | Deployed frontend origin for CORS (default: `http://localhost:5173`) |

## Local Development

```bash
export OPENAI_API_KEY=sk-...
export DATABASE_URL='jdbc:postgresql://db.<project>.supabase.co:6543/postgres'
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
./mvnw spring-boot:run
```

Frontend (separate repo/directory):
```bash
cd frontend && npm install
echo "VITE_API_URL=http://localhost:8080" > .env.local
npm run dev  # http://localhost:5173
```

## Tech Stack

- **Java 17**, Spring Boot 4.0.6
- **Spring AI** with OpenAI provider (`gpt-4o-mini`)
- **Spring Data JPA** + Hibernate, Supabase Postgres
- **Frontend:** Vite + React (deployed to Vercel)
- **Backend host:** Render (free tier — 30–60s cold start after 15 min inactivity)
- **Database host:** Supabase (free tier — pauses after 7 days inactivity)