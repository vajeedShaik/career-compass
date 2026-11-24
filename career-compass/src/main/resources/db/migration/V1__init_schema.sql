CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL DEFAULT 'CANDIDATE',
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE resumes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_filename VARCHAR(255),
    raw_text        TEXT NOT NULL,
    extracted_skills TEXT[],
    embedding       vector(384),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE job_descriptions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255),
    raw_text        TEXT NOT NULL,
    required_skills TEXT[],
    embedding       vector(384),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE matches (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resume_id       UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    jd_id           UUID NOT NULL REFERENCES job_descriptions(id) ON DELETE CASCADE,
    match_score     NUMERIC(5,2) NOT NULL,
    missing_skills  TEXT[],
    matched_skills  TEXT[],
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE roadmaps (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id        UUID NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chat_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    roadmap_id      UUID NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_resumes_user_id ON resumes(user_id);
CREATE INDEX idx_jd_user_id ON job_descriptions(user_id);
CREATE INDEX idx_matches_resume_jd ON matches(resume_id, jd_id);
CREATE INDEX idx_chat_roadmap_id ON chat_messages(roadmap_id);
