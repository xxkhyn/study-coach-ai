package com.studycoachai.dto;

import java.math.BigDecimal;

public record FieldAccuracyResponse(
        String field,
        Integer solvedCount,
        Integer correctCount,
        BigDecimal accuracyRate
) {
}
