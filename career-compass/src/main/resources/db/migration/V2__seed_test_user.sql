-- Temporary seed data for local development only.
-- Lets you test resume/JD upload endpoints before real signup/login (Module 7) exists.
-- This user_id is what you should paste into Swagger's "userId" field.

INSERT INTO users (id, email, password_hash, role)
VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    'test.user@example.com',
    'not-a-real-hash-yet',
    'CANDIDATE'
);
