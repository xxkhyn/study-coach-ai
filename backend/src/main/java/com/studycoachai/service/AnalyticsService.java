package com.studycoachai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.studycoachai.dto.DailyStudyTimeResponse;
import com.studycoachai.dto.FieldAccuracyResponse;
import com.studycoachai.dto.TargetStudyMinutesResponse;
import com.studycoachai.dto.WeakFieldResponse;
import com.studycoachai.entity.QuestionLog;
import com.studycoachai.entity.StudyLog;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.repository.QuestionLogRepository;
import com.studycoachai.repository.StudyLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private static final int WEAK_FIELD_LIMIT = 10;

    private final StudyLogRepository studyLogRepository;
    private final QuestionLogRepository questionLogRepository;

    public AnalyticsService(StudyLogRepository studyLogRepository, QuestionLogRepository questionLogRepository) {
        this.studyLogRepository = studyLogRepository;
        this.questionLogRepository = questionLogRepository;
    }

    @Transactional(readOnly = true)
    public List<DailyStudyTimeResponse> dailyStudyTime(Long userId) {
        return studyLogRepository.findByUserIdOrderByStudiedDateDescCreatedAtDesc(userId).stream()
                .collect(Collectors.groupingBy(StudyLog::getStudiedDate, Collectors.summingInt(this::safeMinutes)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DailyStudyTimeResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TargetStudyMinutesResponse> studyTimeByTarget(Long userId) {
        return studyLogRepository.findByUserIdOrderByStudiedDateDescCreatedAtDesc(userId).stream()
                .collect(Collectors.groupingBy(StudyLog::getStudyTargetId))
                .entrySet()
                .stream()
                .map(entry -> toTargetStudyMinutes(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TargetStudyMinutesResponse::totalMinutes).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FieldAccuracyResponse> accuracyByField(Long userId) {
        return buildFieldAccuracies(questionLogRepository.findByUserIdOrderByPracticedDateDescCreatedAtDesc(userId));
    }

    @Transactional(readOnly = true)
    public List<WeakFieldResponse> weakFields(Long userId) {
        return accuracyByField(userId).stream()
                .filter(field -> field.solvedCount() > 0)
                .sorted(Comparator.comparing(FieldAccuracyResponse::accuracyRate))
                .limit(WEAK_FIELD_LIMIT)
                .map(field -> new WeakFieldResponse(field.field(), field.solvedCount(), field.correctCount(), field.accuracyRate()))
                .toList();
    }

    private List<FieldAccuracyResponse> buildFieldAccuracies(List<QuestionLog> questionLogs) {
        return questionLogs.stream()
                .collect(Collectors.groupingBy(log -> normalizeField(log.getField())))
                .entrySet()
                .stream()
                .map(this::toFieldAccuracy)
                .sorted(Comparator.comparing(FieldAccuracyResponse::accuracyRate))
                .toList();
    }

    private TargetStudyMinutesResponse toTargetStudyMinutes(Long studyTargetId, List<StudyLog> logs) {
        int totalMinutes = logs.stream().map(this::safeMinutes).reduce(0, Integer::sum);
        String targetName = logs.stream()
                .map(StudyLog::getStudyTarget)
                .filter(target -> target != null)
                .map(StudyTarget::getName)
                .findFirst()
                .orElse(null);
        return new TargetStudyMinutesResponse(studyTargetId, targetName, totalMinutes);
    }

    private FieldAccuracyResponse toFieldAccuracy(Map.Entry<String, List<QuestionLog>> entry) {
        int solvedCount = entry.getValue().stream().map(this::safeSolvedCount).reduce(0, Integer::sum);
        int correctCount = entry.getValue().stream().map(this::safeCorrectCount).reduce(0, Integer::sum);
        BigDecimal accuracyRate = solvedCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(correctCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(solvedCount), 2, RoundingMode.HALF_UP);
        return new FieldAccuracyResponse(entry.getKey(), solvedCount, correctCount, accuracyRate);
    }

    private int safeMinutes(StudyLog log) {
        return log.getMinutes() == null ? 0 : log.getMinutes();
    }

    private int safeSolvedCount(QuestionLog log) {
        return log.getSolvedCount() == null ? 0 : log.getSolvedCount();
    }

    private int safeCorrectCount(QuestionLog log) {
        return log.getCorrectCount() == null ? 0 : log.getCorrectCount();
    }

    private String normalizeField(String field) {
        return field == null || field.isBlank() ? "未設定" : field;
    }
}
