# Deploying Career Compass

This deploys the database on **Supabase** (permanent free tier, pgvector built in,
no card required) and the Spring Boot app on **Render** (free web service tier),
via the Dockerfile in this repo.

## Part 1: Database (Supabase)

1. Go to https://supabase.com and sign up (no card needed for the free tier)
2. Create a new project — pick any name/region, set a strong database password
   and **save it**, you'll need it below
3. Once the project is ready, go to **Project Settings → Database**
4. Under **Connection info**, note down:
    - Host
    - Port (usually 5432, or 6543 if using the pooled connection — use the
      direct 5432 connection for this app, not the pooler)
    - Database name (usually `postgres`)
    - User (usually `postgres`)
5. **pgvector**: Supabase supports it natively — you don't need to enable
   anything manually. This project's own `V1__init_schema.sql` migration runs
   `CREATE EXTENSION IF NOT EXISTS vector;` automatically on first startup.

You now have everything needed for the `DB_*` environment variables.

## Part 2: App hosting (Render)

1. Push this project to a GitHub repository if it isn't already there
2. Go to https://render.com and sign up (free tier available, no card for
   the free web service tier)
3. **New → Web Service** → connect your GitHub repo
4. Render will detect the `Dockerfile` automatically — select **Docker** as
   the environment if asked
5. Under **Environment**, add every variable listed in `.env.example`

6. Click **Create Web Service** — Render will build the Docker image and deploy it
7. First deploy takes a few minutes. Watch the **Logs** tab for Flyway migration
   output and `Started CareerCompassApplication` to confirm success

## Verifying the deployment

```bash
curl https://career-compass-xxxx.onrender.com/docs
```

### A note on the free tier

Render's free web services **sleep after 15 minutes of inactivity** and take
~30-60 seconds to wake up on the next request.

This project now uses **Jina AI's hosted embedding API** instead of a local
embedding model, eliminating the large memory usage that previously caused
out-of-memory crashes on Render's free 512 MB instances.

The backend is therefore suitable for deployment on Render's free tier.
## Testing the full stack locally with Docker first (recommended)

```bash
export GROQ_API_KEY=your-key
export JINA_API_KEY=your-jina-api-key
export JINA_DIMENSIONS=384
export MAIL_USERNAME=your-smtp-username
export MAIL_PASSWORD=your-smtp-password
docker compose up --build
```