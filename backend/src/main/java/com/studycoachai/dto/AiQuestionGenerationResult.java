package com.studycoachai.dto;

import java.util.List;

public record AiQuestionGenerationResult(
        String prompt,
        String rawResponse,
        List<AiGeneratedQuestionResponse> questions
) {
}
