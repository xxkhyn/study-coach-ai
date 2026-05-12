package com.studycoachai.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.AiAdviceLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAdviceLogRepository extends JpaRepository<AiAdviceLog, Long> {
    Optional<AiAdviceLog> findFirstByUserIdAndAdviceDateOrderByCreatedAtDesc(Long userId, LocalDate adviceDate);

    List<AiAdviceLog> findByUserIdOrderByAdviceDateDescCreatedAtDesc(Long userId);
}
