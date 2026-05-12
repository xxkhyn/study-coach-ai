package com.studycoachai.repository;

import java.util.List;
import java.util.Optional;

import com.studycoachai.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Question> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndExamTypeAndYearAndSeasonAndTimeCategoryAndQuestionNumber(
            Long userId,
            String examType,
            Integer year,
            String season,
            String timeCategory,
            String questionNumber
    );
}
