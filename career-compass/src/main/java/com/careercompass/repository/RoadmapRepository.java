package com.careercompass.repository;

import com.careercompass.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {
    List<Roadmap> findByMatchIdIn(List<UUID> matchIds);
}