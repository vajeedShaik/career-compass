package com.careercompass.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Handles pgvector "vector" column operations directly via JdbcTemplate.
 * Plain JPA/Spring Data repositories require binding to a managed entity
 * type, which doesn't fit these cross-table, native-SQL-only operations —
 * a plain JDBC DAO is the simpler, more honest tool for this job.
 */
@Repository
public class EmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmbeddingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void updateResumeEmbedding(UUID id, String embeddingLiteral) {
        jdbcTemplate.update(
                "UPDATE resumes SET embedding = CAST(? AS vector) WHERE id = ?",
                embeddingLiteral, id);
    }

    public void updateJdEmbedding(UUID id, String embeddingLiteral) {
        jdbcTemplate.update(
                "UPDATE job_descriptions SET embedding = CAST(? AS vector) WHERE id = ?",
                embeddingLiteral, id);
    }

    /**
     * pgvector's <=> operator returns cosine DISTANCE (0 = identical,
     * larger = more different). Converted to a similarity value here by
     * subtracting from 1; the caller scales it to a 0-100 percentage.
     */
    public Double calculateCosineSimilarity(UUID resumeId, UUID jdId) {
        String sql = """
                SELECT 1 - (r.embedding <=> j.embedding)
                FROM resumes r, job_descriptions j
                WHERE r.id = ? AND j.id = ?
                """;
        return jdbcTemplate.queryForObject(sql, Double.class, resumeId, jdId);
    }
}
