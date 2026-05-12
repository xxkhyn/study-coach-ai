package com.studycoachai.repository;

import java.util.List;

import com.studycoachai.entity.QuestionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionLogRepository extends JpaRepository<QuestionLog, Long> {
    List<QuestionLog> findByUserIdOrderByPracticedDateDescCreatedAtDesc(Long userId);
}
