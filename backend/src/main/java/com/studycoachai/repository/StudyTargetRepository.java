package com.studycoachai.repository;

import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.StudyTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTargetRepository extends JpaRepository<StudyTarget, Long> {
    List<StudyTarget> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<StudyTarget> findByIdAndUserId(Long id, Long userId);
}
