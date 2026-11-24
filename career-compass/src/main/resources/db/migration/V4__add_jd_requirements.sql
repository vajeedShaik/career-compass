-- Adds structured requirement fields to job_descriptions, mirroring the
-- resumes table (education_level / years_experience) so gap analysis in
-- Module 3 can compare like-for-like structured fields, not just skills.

ALTER TABLE job_descriptions
    ADD COLUMN min_education_level   VARCHAR(100),
    ADD COLUMN min_years_experience  NUMERIC(4,1);
