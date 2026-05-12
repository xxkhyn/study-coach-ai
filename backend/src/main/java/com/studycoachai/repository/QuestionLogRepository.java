package com.studycoachai.repository;

import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.QuestionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionLogRepository extends JpaRepository<QuestionLog, Long> {
    List<QuestionLog> findByUserIdOrderByPracticedDateDescCreatedAtDesc(Long userId);

    Optional<QuestionLog> findByIdAndUserId(Long id, Long userId);
}
