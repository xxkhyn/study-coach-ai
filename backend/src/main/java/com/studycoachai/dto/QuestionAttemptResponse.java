package com.studycoachai.dto;

import java.time.OffsetDateTime;

public record QuestionAttemptResponse(
        Long id,
        Long userId,
        Long questionId,
        String questionText,
        String studyTargetName,
        String field,
        Integer selectedIndex,
        Integer answerIndex,
        Boolean correct,
        OffsetDateTime answeredAt
) {
}
