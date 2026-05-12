package com.studycoachai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.studycoachai.dto.FieldAccuracyResponse;
import com.studycoachai.dto.QuestionLogRequest;
import com.studycoachai.dto.QuestionLogResponse;
import com.studycoachai.dto.WeakFieldResponse;
import com.studycoachai.entity.QuestionLog;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.QuestionLogRepository;
import com.studycoachai.repository.StudyTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionLogService {
    private static final int WEAK_FIELD_LIMIT = 10;

    private final QuestionLogRepository questionLogRepository;
    private final StudyTargetRepository studyTargetRepository;

    public QuestionLogService(
            QuestionLogRepository questionLogRepository,
            StudyTargetRepository studyTargetRepository
    ) {
        this.questionLogRepository = questionLogRepository;
        this.studyTargetRepository = studyTargetRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionLogResponse> list(Long userId) {
        return questionLogRepository.findByUserIdOrderByPracticedDateDescCreatedAtDesc(userId).stream()
                .map(QuestionLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionLogResponse get(Long userId, Long id) {
        return QuestionLogResponse.from(findLog(userId, id));
    }

    @Transactional
    public QuestionLogResponse create(Long userId, QuestionLogRequest request) {
        validateCounts(request);
        StudyTarget target = findTarget(userId, request.studyTargetId());
        QuestionLog log = new QuestionLog(
                userId,
                target.getId(),
                request.field(),
                request.practicedDate(),
                request.solvedCount(),
                request.correctCount(),
                request.memo()
        );
        return QuestionLogResponse.from(questionLogRepository.save(log));
    }

    @Transactional
    public QuestionLogResponse update(Long userId, Long id, QuestionLogRequest request) {
        validateCounts(request);
        QuestionLog log = findLog(userId, id);
        StudyTarget target = findTarget(userId, request.studyTargetId());
        log.update(
                target.getId(),
                request.field(),
                request.practicedDate(),
                request.solvedCount(),
                request.correctCount(),
                request.memo()
        );
        return QuestionLogResponse.from(log);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        questionLogRepository.delete(findLog(userId, id));
    }

    @Transactional(readOnly = true)
    public List<FieldAccuracyResponse> accuracyByField(Long userId) {
        return questionLogRepository.findByUserIdOrderByPracticedDateDescCreatedAtDesc(userId).stream()
                .collect(Collectors.groupingBy(log -> normalizeField(log.getField())))
                .entrySet()
                .stream()
                .map(this::toFieldAccuracy)
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

    private void validateCounts(QuestionLogRequest request) {
        if (request.correctCount() > request.solvedCount()) {
            throw new IllegalArgumentException("Correct count cannot exceed solved count.");
        }
    }

    private FieldAccuracyResponse toFieldAccuracy(Map.Entry<String, List<QuestionLog>> entry) {
        int solvedCount = entry.getValue().stream().map(this::safeSolvedCount).reduce(0, Integer::sum);
        int correctCount = entry.getValue().stream().map(this::safeCorrectCount).reduce(0, Integer::sum);
        BigDecimal accuracyRate = solvedCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(correctCount).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(solvedCount), 2, RoundingMode.HALF_UP);
        return new FieldAccuracyResponse(entry.getKey(), solvedCount, correctCount, accuracyRate);
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

    private StudyTarget findTarget(Long userId, Long studyTargetId) {
        return studyTargetRepository.findByIdAndUserId(studyTargetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + studyTargetId));
    }

    private QuestionLog findLog(Long userId, Long id) {
        return questionLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Question log not found: " + id));
    }
}
