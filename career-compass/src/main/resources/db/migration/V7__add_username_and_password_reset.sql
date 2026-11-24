-- Adds username, backfilled from email prefix for existing users so the
-- NOT NULL + UNIQUE constraint can be applied safely without breaking
-- existing rows.
ALTER TABLE users ADD COLUMN username VARCHAR(100);
UPDATE users SET username = split_part(email, '@', 1) WHERE username IS NULL;
ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_username_unique UNIQUE (username);

CREATE TABLE password_reset_tokens (
                                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                       token       VARCHAR(255) NOT NULL UNIQUE,
                                       expires_at  TIMESTAMP NOT NULL,
                                       used        BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);