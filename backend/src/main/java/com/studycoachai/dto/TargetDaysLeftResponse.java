package com.studycoachai.dto;

public record TargetDaysLeftResponse(
        Long studyTargetId,
        String studyTargetName,
        Long daysLeft
) {
}
