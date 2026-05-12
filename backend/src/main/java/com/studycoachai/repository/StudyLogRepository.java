package com.studycoachai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.StudyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
    List<StudyLog> findByUserIdOrderByStudiedDateDescCreatedAtDesc(Long userId);

    Optional<StudyLog> findByIdAndUserId(Long id, Long userId);

    List<StudyLog> findByUserIdAndStudyTargetIdOrderByStudiedDateDescCreatedAtDesc(Long userId, Long studyTargetId);

    List<StudyLog> findByUserIdAndStudiedDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
