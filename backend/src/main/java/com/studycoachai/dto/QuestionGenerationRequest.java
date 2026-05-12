package com.studycoachai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionGenerationRequest(
        @NotNull Long studyTargetId,
        @NotBlank @Size(max = 80) String examType,
        @NotBlank @Size(max = 80) String field,
        @NotBlank @Size(max = 40) String difficulty,
        @NotNull @Min(1) @Max(10) Integer count
) {
}
