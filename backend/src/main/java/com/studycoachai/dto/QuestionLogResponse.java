package com.studycoachai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.studycoachai.entity.QuestionLog;

public record QuestionLogResponse(
        Long id,
        Long userId,
        Long studyTargetId,
        String studyTargetName,
        String field,
        LocalDate practicedDate,
        Integer solvedCount,
        Integer correctCount,
        BigDecimal accuracyRate,
        String memo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static QuestionLogResponse from(QuestionLog log) {
        return new QuestionLogResponse(
                log.getId(),
                log.getUserId(),
                log.getStudyTargetId(),
                log.getStudyTarget() == null ? null : log.getStudyTarget().getName(),
                log.getField(),
                log.getPracticedDate(),
                log.getSolvedCount(),
                log.getCorrectCount(),
                log.getAccuracyRate(),
                log.getMemo(),
                log.getCreatedAt(),
                log.getUpdatedAt()
        );
    }
}
