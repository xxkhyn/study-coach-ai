package com.studycoachai.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudyLogRequest(
        @NotNull Long studyTargetId,
        @Size(max = 80) String field,
        @NotNull LocalDate studiedDate,
        @NotNull @Min(1) Integer minutes,
        @Size(max = 1000) String memo
) {
}
