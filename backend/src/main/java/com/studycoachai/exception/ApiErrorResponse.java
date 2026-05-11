package com.studycoachai.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        List<String> details
) {
    public static ApiErrorResponse of(int status, String error, List<String> details) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, error, details);
    }
}
