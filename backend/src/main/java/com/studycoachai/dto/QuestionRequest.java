package com.studycoachai.dto;

import java.util.List;

import com.studycoachai.entity.Question.SourceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotNull Long studyTargetId,
        @Size(max = 80) String examType,
        @Min(1900) @Max(2100) Integer year,
        @Size(max = 40) String season,
        @Size(max = 40) String timeCategory,
        @Size(max = 40) String questionNumber,
        @Size(max = 80) String field,
        @Size(max = 40) String difficulty,
        @NotBlank @Size(max = 4000) String questionText,
        @NotNull @Size(min = 4, max = 4) List<@NotBlank @Size(max = 1000) String> choices,
        @NotNull @Min(0) @Max(3) Integer answerIndex,
        @Size(max = 4000) String explanation,
        SourceType sourceType,
        @Size(max = 200) String sourceLabel,
        @Size(max = 1000) String sourceUrl
) {
}
