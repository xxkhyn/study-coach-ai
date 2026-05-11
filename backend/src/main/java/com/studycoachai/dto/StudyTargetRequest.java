package com.studycoachai.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyTargetRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 80) String category,
        LocalDate examDate,
        LocalDate goalDate,
        @Size(max = 1000) String memo
) {
}
