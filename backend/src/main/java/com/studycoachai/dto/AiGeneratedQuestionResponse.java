package com.studycoachai.dto;

import java.util.List;

public record AiGeneratedQuestionResponse(
        String field,
        String difficulty,
        String questionText,
        List<String> choices,
        Integer answerIndex,
        String explanation
) {
}
