package com.studycoachai.dto;

import java.time.LocalDate;
import java.util.List;

public record AiAdvicePromptData(
        LocalDate today,
        List<StudyTargetResponse> studyTargets,
        List<TargetDaysLeftResponse> targetDaysLeft,
        List<StudyTaskResponse> todayTasks,
        List<StudyTaskResponse> overdueTasks,
        List<StudyLogResponse> recentSevenDayStudyLogs,
        List<FieldAccuracyResponse> fieldAccuracies,
        List<WeakFieldResponse> weakFields,
        List<QuestionLogResponse> recentQuestionLogs
) {
}
