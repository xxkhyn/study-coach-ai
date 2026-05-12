package com.studycoachai.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.studycoachai.entity.Question.SourceType;

public record QuestionResponse(
        Long id,
        Long userId,
        Long studyTargetId,
        String studyTargetName,
        String examType,
        Integer year,
        String season,
        String timeCategory,
        String questionNumber,
        String field,
        String difficulty,
        String questionText,
        List<String> choices,
        Integer answerIndex,
        String explanation,
        SourceType sourceType,
        String sourceLabel,
        String sourceUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
