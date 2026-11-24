package com.careercompass.repository;

import com.careercompass.entity.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LearningResourceRepository extends JpaRepository<LearningResource, UUID> {
    // Skill matching is done in RoadmapService via normalized comparison,
    // not exact SQL lookup — see SkillNameMatcher. The resource table is
    // small, so loading all rows once per request is simple and fast.
}
