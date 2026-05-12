package com.studycoachai.dto;

import java.util.List;

public record QuestionImportResultResponse(
        int importedCount,
        int skippedCount,
        int errorCount,
        List<String> errors
) {
}
