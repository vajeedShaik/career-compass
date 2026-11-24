-- Adds fields needed for the personalized report. candidate_name is new:
-- earlier design deliberately excluded personal details from scoring (still
-- true — this is never used in match logic), but the report is emailed
-- back to the resume's own owner, so displaying their name in their own
-- report is a reasonable, low-risk use, unlike the original recruiter-facing
-- screening use case this project started from.

ALTER TABLE resumes
    ADD COLUMN candidate_name   VARCHAR(255),
    ADD COLUMN certifications   TEXT[],
    ADD COLUMN soft_skills      TEXT[];

ALTER TABLE job_descriptions
    ADD COLUMN company_name     VARCHAR(255);
