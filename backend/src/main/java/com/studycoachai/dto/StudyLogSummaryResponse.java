package com.studycoachai.dto;

import java.time.LocalDate;
import java.util.List;

public record StudyLogSummaryResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        Integer totalMinutes,
        List<TargetStudyMinutesResponse> targetSummaries,
        List<FieldStudyMinutesResponse> fieldSummaries
) {
}
