package com.careercompass.repository;

import com.careercompass.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("SELECT m FROM Match m WHERE m.resumeId IN (SELECT r.id FROM Resume r WHERE r.userId = :userId)")
    List<Match> findByUserId(UUID userId);
}