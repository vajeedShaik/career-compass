-- Extends resumes with structured fields beyond the flat skill list,
-- so eligibility judgment isn't based on semantic similarity alone.

ALTER TABLE resumes
    ADD COLUMN education_level      VARCHAR(100),
    ADD COLUMN years_experience     NUMERIC(4,1),
    ADD COLUMN projects_json        TEXT;

-- projects_json stores a JSON array like:
-- [{"name": "...", "description": "...", "technologies": ["Java", "Spring Boot"]}]
-- Stored as TEXT rather than a native JSONB column to avoid pulling in an
-- extra Hibernate JSON-type dependency for this stage of the project;
-- parsed/serialized in Java via Jackson's ObjectMapper where needed.
