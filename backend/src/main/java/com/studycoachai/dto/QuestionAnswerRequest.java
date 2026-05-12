package com.studycoachai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuestionAnswerRequest(
        @NotNull @Min(0) @Max(3) Integer selectedIndex
) {
}
