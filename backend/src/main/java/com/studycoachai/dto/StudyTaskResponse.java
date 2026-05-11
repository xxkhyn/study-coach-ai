package com.studycoachai.dto;

import java.time.LocalDate;

import com.studycoachai.entity.StudyTask;

public record StudyTaskResponse(
        Long id,
        Long userId,
        Long targetId,
        String targetName,
        String title,
        String fieldName,
        Integer plannedMinutes,
        LocalDate dueDate,
        boolean completed
) {
    public static StudyTaskResponse from(StudyTask task) {
        return new StudyTaskResponse(
                task.getId(),
                task.getUser().getId(),
                task.getStudyTarget().getId(),
                task.getStudyTarget().getName(),
                task.getTitle(),
                task.getFieldName(),
                task.getPlannedMinutes(),
                task.getDueDate(),
                task.isCompleted()
        );
    }
}
