package com.studycoachai.repository;

import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findByUserIdOrderByCompletedAscDueDateAscCreatedAtDesc(Long userId);

    Optional<StudyTask> findByIdAndUserId(Long id, Long userId);
}
