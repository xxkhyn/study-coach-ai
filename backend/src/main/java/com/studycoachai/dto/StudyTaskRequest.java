package com.studycoachai.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudyTaskRequest(
        @NotNull Long studyTargetId,
        @NotBlank @Size(max = 160) String title,
        @Size(max = 80) String field,
        @Min(0) Integer plannedMinutes,
        LocalDate dueDate,
        boolean completed
) {
}
