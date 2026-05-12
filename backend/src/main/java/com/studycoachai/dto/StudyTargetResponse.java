package com.studycoachai.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.studycoachai.entity.StudyTarget;

public record StudyTargetResponse(
        Long id,
        Long userId,
        String name,
        String description,
        LocalDate targetDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static StudyTargetResponse from(StudyTarget target) {
        return new StudyTargetResponse(
                target.getId(),
                target.getUserId(),
                target.getName(),
                target.getDescription(),
                target.getTargetDate(),
                target.getCreatedAt(),
                target.getUpdatedAt()
        );
    }
}
