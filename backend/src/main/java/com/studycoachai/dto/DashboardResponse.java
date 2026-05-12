package com.studycoachai.dto;

import java.util.List;

public record DashboardResponse(
        List<StudyTaskResponse> todayTasks,
        List<StudyTaskResponse> overdueTasks,
        StudyLogSummaryResponse weeklyStudySummary,
        List<FieldAccuracyResponse> fieldAccuracies,
        List<WeakFieldResponse> weakFields,
        List<StudyLogResponse> recentStudyLogs,
        List<QuestionLogResponse> recentQuestionLogs
) {
}
