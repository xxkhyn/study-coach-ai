package com.studycoachai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findByUserIdOrderByCompletedAscDueDateAscCreatedAtDesc(Long userId);

    List<StudyTask> findByUserIdAndCompletedFalseAndDueDate(Long userId, LocalDate dueDate);

    List<StudyTask> findByUserIdAndCompletedFalseAndDueDateBeforeOrderByDueDateAsc(Long userId, LocalDate dueDate);

    Optional<StudyTask> findByIdAndUserId(Long id, Long userId);
}
