package com.studycoachai.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.studycoachai.entity.StudyTask;

public record StudyTaskResponse(
        Long id,
        Long userId,
        Long studyTargetId,
        String studyTargetName,
        String title,
        String field,
        Integer plannedMinutes,
        LocalDate dueDate,
        boolean completed,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static StudyTaskResponse from(StudyTask task) {
        return new StudyTaskResponse(
                task.getId(),
                task.getUserId(),
                task.getStudyTargetId(),
                task.getStudyTarget() == null ? null : task.getStudyTarget().getName(),
                task.getTitle(),
                task.getField(),
                task.getPlannedMinutes(),
                task.getDueDate(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
