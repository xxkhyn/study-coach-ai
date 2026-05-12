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
import com.studycoachai.entity.StudyLog;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.repository.QuestionAttemptRepository;
import com.studycoachai.repository.QuestionLogRepository;
import com.studycoachai.repository.StudyLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private static final int WEAK_FIELD_LIMIT = 10;

    private final StudyLogRepository studyLogRepository;
    private final QuestionLogRepository questionLogRepository;
    private final QuestionAttemptRepository questionAttemptRepository;

    public AnalyticsService(
            StudyLogRepository studyLogRepository,
            QuestionLogRepository questionLogRepository,
            QuestionAttemptRepository questionAttemptRepository
    ) {
        this.studyLogRepository = studyLogRepository;
        this.questionLogRepository = questionLogRepository;
        this.questionAttemptRepository = questionAttemptRepository;
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
        Map<String, AccuracyCounts> countsByField = questionLogRepository.findByUserIdOrderByPracticedDateDescCreatedAtDesc(userId).stream()
                .collect(Collectors.toMap(
                        log -> normalizeField(log.getField()),
                        log -> new AccuracyCounts(safeSolvedCount(log), safeCorrectCount(log)),
                        AccuracyCounts::merge
                ));

        questionAttemptRepository.findByUserIdOrderByAnsweredAtDesc(userId).stream()
                .filter(attempt -> attempt.getQuestion() != null)
                .forEach(attempt -> countsByField.merge(
                        normalizeField(attempt.getQuestion().getField()),
                        new AccuracyCounts(1, Boolean.TRUE.equals(attempt.getCorrect()) ? 1 : 0),
                        AccuracyCounts::merge
                ));

        return countsByField.entrySet().stream()
                .map(entry -> toFieldAccuracy(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(FieldAccuracyResponse::accuracyRate))
                .toList();
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

    private FieldAccuracyResponse toFieldAccuracy(String field, AccuracyCounts counts) {
        int solvedCount = counts.solvedCount();
        int correctCount = counts.correctCount();
        BigDecimal accuracyRate = solvedCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(correctCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(solvedCount), 2, RoundingMode.HALF_UP);
        return new FieldAccuracyResponse(field, solvedCount, correctCount, accuracyRate);
    }

    private int safeMinutes(StudyLog log) {
        return log.getMinutes() == null ? 0 : log.getMinutes();
    }

    private int safeSolvedCount(com.studycoachai.entity.QuestionLog log) {
        return log.getSolvedCount() == null ? 0 : log.getSolvedCount();
    }

    private int safeCorrectCount(com.studycoachai.entity.QuestionLog log) {
        return log.getCorrectCount() == null ? 0 : log.getCorrectCount();
    }

    private String normalizeField(String field) {
        return field == null || field.isBlank() ? "未設定" : field;
    }

    private record AccuracyCounts(int solvedCount, int correctCount) {
        private AccuracyCounts merge(AccuracyCounts other) {
            return new AccuracyCounts(solvedCount + other.solvedCount, correctCount + other.correctCount);
        }
    }
}
