package com.studycoachai.dto;

public record QuestionGenerationPromptData(
        String studyTargetName,
        String examType,
        String field,
        String difficulty,
        Integer count
) {
}
