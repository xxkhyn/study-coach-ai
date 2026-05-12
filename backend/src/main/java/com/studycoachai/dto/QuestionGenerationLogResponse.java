package com.studycoachai.dto;

import java.time.OffsetDateTime;

import com.studycoachai.entity.QuestionGenerationLog;

public record QuestionGenerationLogResponse(
        Long id,
        Long userId,
        Long studyTargetId,
        String studyTargetName,
        String examType,
        String field,
        String difficulty,
        Integer count,
        OffsetDateTime createdAt
) {
    public static QuestionGenerationLogResponse from(QuestionGenerationLog log) {
        return new QuestionGenerationLogResponse(
                log.getId(),
                log.getUserId(),
                log.getStudyTargetId(),
                log.getStudyTarget() == null ? null : log.getStudyTarget().getName(),
                log.getExamType(),
                log.getField(),
                log.getDifficulty(),
                log.getCount(),
                log.getCreatedAt()
        );
    }
}
