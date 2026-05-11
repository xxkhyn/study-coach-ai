package com.studycoachai.dto;

import java.time.LocalDate;

import com.studycoachai.entity.StudyTarget;

public record StudyTargetResponse(
        Long id,
        Long userId,
        String name,
        String category,
        LocalDate examDate,
        LocalDate goalDate,
        String memo
) {
    public static StudyTargetResponse from(StudyTarget target) {
        return new StudyTargetResponse(
                target.getId(),
                target.getUser().getId(),
                target.getName(),
                target.getCategory(),
                target.getExamDate(),
                target.getGoalDate(),
                target.getMemo()
        );
    }
}
