package com.studycoachai.dto;

import java.time.LocalDate;

public record DailyStudyTimeResponse(
        LocalDate studiedDate,
        Integer totalMinutes
) {
}
