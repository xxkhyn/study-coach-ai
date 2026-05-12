package com.studycoachai.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycoachai.ai.AiClient;
import com.studycoachai.dto.AiAdvicePromptData;
import com.studycoachai.dto.AiAdviceRequest;
import com.studycoachai.dto.AiAdviceResponse;
import com.studycoachai.dto.AiAdviceTaskResponse;
import com.studycoachai.dto.AiAdviceWeakPointResponse;
import com.studycoachai.dto.DashboardResponse;
import com.studycoachai.dto.StudyLogResponse;
import com.studycoachai.dto.StudyTargetResponse;
import com.studycoachai.dto.TargetDaysLeftResponse;
import com.studycoachai.entity.AiAdviceLog;
import com.studycoachai.repository.AiAdviceLogRepository;
import com.studycoachai.repository.StudyLogRepository;
import com.studycoachai.repository.StudyTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiAdviceService {
    private final AiClient aiClient;
    private final AiAdviceLogRepository aiAdviceLogRepository;
    private final StudyTargetRepository studyTargetRepository;
    private final StudyLogRepository studyLogRepository;
    private final DashboardService dashboardService;
    private final ObjectMapper objectMapper;

    public AiAdviceService(
            AiClient aiClient,
            AiAdviceLogRepository aiAdviceLogRepository,
            StudyTargetRepository studyTargetRepository,
            StudyLogRepository studyLogRepository,
            DashboardService dashboardService,
            ObjectMapper objectMapper
    ) {
        this.aiClient = aiClient;
        this.aiAdviceLogRepository = aiAdviceLogRepository;
        this.studyTargetRepository = studyTargetRepository;
        this.studyLogRepository = studyLogRepository;
        this.dashboardService = dashboardService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiAdviceResponse generateDailyAdvice(Long userId, AiAdviceRequest request) {
        LocalDate today = LocalDate.now();
        if (request == null || !request.shouldForce()) {
            return aiAdviceLogRepository.findFirstByUserIdAndAdviceDateOrderByCreatedAtDesc(userId, today)
                    .map(this::toResponse)
                    .orElseGet(() -> generateAndSave(userId, today));
        }
        return generateAndSave(userId, today);
    }

    @Transactional(readOnly = true)
    public AiAdviceResponse getTodayAdvice(Long userId) {
        return aiAdviceLogRepository.findFirstByUserIdAndAdviceDateOrderByCreatedAtDesc(userId, LocalDate.now())
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AiAdviceResponse> history(Long userId) {
        return aiAdviceLogRepository.findByUserIdOrderByAdviceDateDescCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AiAdviceResponse generateAndSave(Long userId, LocalDate today) {
        AiAdviceResponse generatedAdvice = aiClient.generateDailyAdvice(buildPromptData(userId, today));
        AiAdviceLog log = new AiAdviceLog(
                userId,
                today,
                generatedAdvice.summary(),
                writeJson(generatedAdvice.tasks()),
                writeJson(generatedAdvice.weakPoints()),
                generatedAdvice.overallAdvice(),
                generatedAdvice.rawResponse()
        );
        return toResponse(aiAdviceLogRepository.save(log));
    }

    private AiAdvicePromptData buildPromptData(Long userId, LocalDate today) {
        DashboardResponse dashboard = dashboardService.getDashboard(userId);
        LocalDate weekAgo = today.minusDays(6);
        List<StudyTargetResponse> targets = studyTargetRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(StudyTargetResponse::from)
                .toList();
        List<StudyLogResponse> recentSevenDayLogs = studyLogRepository
                .findByUserIdAndStudiedDateBetween(userId, weekAgo, today)
                .stream()
                .map(StudyLogResponse::from)
                .toList();

        return new AiAdvicePromptData(
                today,
                targets,
                targets.stream()
                        .map(target -> new TargetDaysLeftResponse(target.id(), target.name(), daysUntil(today, target.targetDate())))
                        .toList(),
                dashboard.todayTasks(),
                dashboard.overdueTasks(),
                recentSevenDayLogs,
                dashboard.fieldAccuracies(),
                dashboard.weakFields(),
                dashboard.recentQuestionLogs()
        );
    }

    private Long daysUntil(LocalDate today, LocalDate targetDate) {
        return targetDate == null ? null : ChronoUnit.DAYS.between(today, targetDate);
    }

    private AiAdviceResponse toResponse(AiAdviceLog log) {
        return new AiAdviceResponse(
                log.getId(),
                log.getUserId(),
                log.getAdviceDate(),
                log.getSummary(),
                readList(log.getTasksJson(), AiAdviceTaskResponse.class),
                readList(log.getWeakPointsJson(), AiAdviceWeakPointResponse.class),
                log.getOverallAdvice(),
                log.getRawResponse(),
                log.getCreatedAt()
        );
    }

    private <T> List<T> readList(String json, Class<T> itemType) {
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, itemType)
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Saved AI advice JSON could not be parsed.", ex);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize AI advice data.", ex);
        }
    }
}
