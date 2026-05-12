package com.studycoachai.repository;

import java.util.List;

import com.studycoachai.entity.QuestionGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionGenerationLogRepository extends JpaRepository<QuestionGenerationLog, Long> {
    List<QuestionGenerationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
