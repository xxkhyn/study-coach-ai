package com.studycoachai.dto;

import java.math.BigDecimal;

public record WeakFieldResponse(
        String field,
        Integer solvedCount,
        Integer correctCount,
        BigDecimal accuracyRate
) {
}
