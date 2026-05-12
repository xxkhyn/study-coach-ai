package com.studycoachai.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionLogRequest(
        @NotNull Long studyTargetId,
        @Size(max = 80) String field,
        @NotNull LocalDate practicedDate,
        @NotNull @Min(0) Integer solvedCount,
        @NotNull @Min(0) Integer correctCount,
        @Size(max = 1000) String memo
) {
}
