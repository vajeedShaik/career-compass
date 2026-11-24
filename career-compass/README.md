# Career Compass

AI-powered resume-to-JD gap analysis and career prep assistant, built with Java + Spring Boot + Spring AI.

## What's implemented in this slice

- Project structure, DB schema (Flyway migration), Docker Compose for Postgres + pgvector
- `POST /api/resumes/upload` — accepts a PDF resume, extracts text (PDFBox), and derives
  a normalized skill list via an LLM call (Spring AI + Anthropic)

This is intentionally a small, working first slice per the MVP plan — Week 1 of the
4-week build plan (see the project plan PDF). Matching, gap analysis, roadmap generation,
and chat come next.

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker (for Postgres + pgvector)
- A free Groq API key (https://console.groq.com/keys)

## Setup

1. **Start the database:**
   ```bash
   docker compose up -d
   ```

2. **Set your API key:**
   ```bash
   export GROQ_API_KEY=your-key-here
   ```
   This uses Groq's free tier (no card required) via Spring AI's OpenAI-compatible
   client, pointed at Groq's API instead of OpenAI's — same code, zero cost.

3. **Run the app:**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify it's up:**
   ```bash
   curl http://localhost:8080/docs
   ```
   Swagger UI is at `http://localhost:8080/docs`.

## Try the upload endpoint

```bash
curl -X POST http://localhost:8080/api/resumes/upload \
  -F "file=@/path/to/resume.pdf" \
  -F "userId=00000000-0000-0000-0000-000000000001"
```

Note: `userId` is a temporary manual param until JWT auth (Module 7) is wired in —
at that point it'll come from the authenticated principal instead.

## Project layout

```
src/main/java/com/careercompass/
  config/       - security, CORS, AI client beans (to be added)
  controller/   - REST endpoints
  dto/          - request/response records
  entity/       - JPA entities
  repository/   - Spring Data repositories
  service/      - business logic (parsing, AI calls, matching)
  exception/    - global error handling
src/main/resources/
  application.yml
  db/migration/ - Flyway SQL migrations
```

## Next modules (in build order)

1. Job description ingestion (`/api/jobs`) — mirrors the resume upload flow
2. Embedding generation + pgvector similarity search (matching engine)
3. Gap analysis service (resume skills vs JD skills diff)
4. RAG-grounded roadmap generation
5. Conversational chat endpoint with context
6. JWT auth (replaces the temporary `userId` param)
7. React frontend

## Running tests

```bash
mvn test
```

**What's covered:**
- `EducationLevelUtilTest`, `SkillNameMatcherTest` — pure logic, no dependencies, fastest to run
- `ResumeParsingServiceTest` — AI response parsing/mapping logic, with the AI call mocked (no API key or cost needed)
- `MatchServiceTest` — the core hybrid scoring logic (embedding similarity + experience/education penalties), fully mocked, including the exact scenario found during manual testing (0 vs 2 years experience)
- `RepositoryIntegrationTest` — real database test using Testcontainers (spins up an actual ephemeral PostgreSQL + pgvector container automatically, no setup needed beyond Docker running). Runs the real Flyway migrations and tests real embedding similarity using the free local model — no AI API key needed for this test file specifically (a dummy key is injected just to let the Spring context start).

**First run note:** the integration test downloads the local embedding model (~90MB) on first use if it isn't already cached from running the app itself — this is normal and only happens once.
