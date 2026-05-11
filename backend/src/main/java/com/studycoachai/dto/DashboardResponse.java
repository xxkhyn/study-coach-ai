package com.studycoachai.dto;

import java.util.List;

public record DashboardResponse(
        long targetCount,
        long taskCount,
        long completedTaskCount,
        long openTaskCount,
        long overdueTaskCount,
        int plannedMinutesThisWeek,
        List<StudyTaskResponse> todayTasks,
        List<StudyTaskResponse> upcomingTasks
) {
}
