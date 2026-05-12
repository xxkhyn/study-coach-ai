package com.studycoachai.repository;

import java.util.List;

import com.studycoachai.entity.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {
    List<QuestionAttempt> findByUserIdOrderByAnsweredAtDesc(Long userId);

    void deleteByUserIdAndQuestionId(Long userId, Long questionId);
}
