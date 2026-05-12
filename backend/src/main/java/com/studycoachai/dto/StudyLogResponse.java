package com.studycoachai.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.studycoachai.entity.StudyLog;

public record StudyLogResponse(
        Long id,
        Long userId,
        Long studyTargetId,
        String studyTargetName,
        String field,
        LocalDate studiedDate,
        Integer minutes,
        String memo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static StudyLogResponse from(StudyLog log) {
        return new StudyLogResponse(
                log.getId(),
                log.getUserId(),
                log.getStudyTargetId(),
                log.getStudyTarget() == null ? null : log.getStudyTarget().getName(),
                log.getField(),
                log.getStudiedDate(),
                log.getMinutes(),
                log.getMemo(),
                log.getCreatedAt(),
                log.getUpdatedAt()
        );
    }
}
