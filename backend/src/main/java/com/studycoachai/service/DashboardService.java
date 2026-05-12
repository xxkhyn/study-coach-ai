package com.studycoachai.service;

import java.time.LocalDate;
import java.util.List;

import com.studycoachai.dto.DashboardResponse;
import com.studycoachai.dto.QuestionLogResponse;
import com.studycoachai.dto.StudyLogResponse;
import com.studycoachai.dto.StudyLogSummaryResponse;
import com.studycoachai.dto.StudyTaskResponse;
import com.studycoachai.repository.QuestionLogRepository;
import com.studycoachai.repository.StudyLogRepository;
import com.studycoachai.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private static final int RECENT_LIMIT = 5;

    private final StudyTaskRepository studyTaskRepository;
    private final StudyLogRepository studyLogRepository;
    private final QuestionLogRepository questionLogRepository;
    private final StudyLogService studyLogService;
    private final AnalyticsService analyticsService;

    public DashboardService(
            StudyTaskRepository studyTaskRepository,
            StudyLogRepository studyLogRepository,
            QuestionLogRepository questionLogRepository,
            StudyLogService studyLogService,
            AnalyticsService analyticsService
    ) {
        this.studyTaskRepository = studyTaskRepository;
        this.studyLogRepository = studyLogRepository;
        this.questionLogRepository = questionLogRepository;
        this.studyLogService = studyLogService;
        this.analyticsService = analyticsService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        LocalDate today = LocalDate.now();
        List<StudyTaskResponse> todayTasks = studyTaskRepository
                .findByUserIdAndCompletedFalseAndDueDate(userId, today)
                .stream()
                .map(StudyTaskResponse::from)
                .toList();
        List<StudyTaskResponse> overdueTasks = studyTaskRepository
                .findByUserIdAndCompletedFalseAndDueDateBeforeOrderByDueDateAsc(userId, today)
                .stream()
                .map(StudyTaskResponse::from)
                .toList();
        StudyLogSummaryResponse weeklySummary = studyLogService.weeklySummary(userId);
        List<StudyLogResponse> recentStudyLogs = studyLogRepository
                .findByUserIdOrderByStudiedDateDescCreatedAtDesc(userId)
                .stream()
                .limit(RECENT_LIMIT)
                .map(StudyLogResponse::from)
                .toList();
        List<QuestionLogResponse> recentQuestionLogs = questionLogRepository
                .findByUserIdOrderByPracticedDateDescCreatedAtDesc(userId)
                .stream()
                .limit(RECENT_LIMIT)
                .map(QuestionLogResponse::from)
                .toList();

        return new DashboardResponse(
                todayTasks,
                overdueTasks,
                weeklySummary,
                analyticsService.accuracyByField(userId),
                analyticsService.weakFields(userId),
                recentStudyLogs,
                recentQuestionLogs
        );
    }
}
