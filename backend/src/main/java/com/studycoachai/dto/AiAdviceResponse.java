package com.studycoachai.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record AiAdviceResponse(
        Long id,
        Long userId,
        LocalDate adviceDate,
        String summary,
        List<AiAdviceTaskResponse> tasks,
        List<AiAdviceWeakPointResponse> weakPoints,
        String overallAdvice,
        String rawResponse,
        OffsetDateTime createdAt
) {
}
