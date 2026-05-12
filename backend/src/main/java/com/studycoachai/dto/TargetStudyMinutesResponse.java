package com.studycoachai.dto;

public record TargetStudyMinutesResponse(
        Long studyTargetId,
        String studyTargetName,
        Integer totalMinutes
) {
}
