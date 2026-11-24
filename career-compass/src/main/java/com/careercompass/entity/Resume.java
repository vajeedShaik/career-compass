package com.careercompass.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "candidate_name")
    private String candidateName;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "extracted_skills")
    private List<String> extractedSkills;

    @Column(name = "soft_skills")
    private List<String> softSkills;

    @Column(name = "certifications")
    private List<String> certifications;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "years_experience")
    private java.math.BigDecimal yearsExperience;

    @Column(name = "projects_json", columnDefinition = "TEXT")
    private String projectsJson;
    // Stored as a raw JSON string (see ParsedResumeData.ProjectData) and
    // serialized/deserialized via Jackson in ResumeService, rather than
    // mapped as a native column type — keeps this entity dependency-free.

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    // Note: the "embedding" vector column is written via a native query in
    // EmbeddingRepository rather than mapped here, since plain JPA does not
    // support the pgvector column type out of the box.
}
