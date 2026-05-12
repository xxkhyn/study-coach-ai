package com.studycoachai.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.studycoachai.dto.FieldStudyMinutesResponse;
import com.studycoachai.dto.StudyLogRequest;
import com.studycoachai.dto.StudyLogResponse;
import com.studycoachai.dto.StudyLogSummaryResponse;
import com.studycoachai.dto.TargetStudyMinutesResponse;
import com.studycoachai.entity.StudyLog;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.StudyLogRepository;
import com.studycoachai.repository.StudyTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyLogService {
    private final StudyLogRepository studyLogRepository;
    private final StudyTargetRepository studyTargetRepository;

    public StudyLogService(StudyLogRepository studyLogRepository, StudyTargetRepository studyTargetRepository) {
        this.studyLogRepository = studyLogRepository;
        this.studyTargetRepository = studyTargetRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyLogResponse> list(Long userId) {
        return studyLogRepository.findByUserIdOrderByStudiedDateDescCreatedAtDesc(userId).stream()
                .map(StudyLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyLogResponse get(Long userId, Long id) {
        return StudyLogResponse.from(findLog(userId, id));
    }

    @Transactional(readOnly = true)
    public List<StudyLogResponse> listByTarget(Long userId, Long studyTargetId) {
        findTarget(userId, studyTargetId);
        return studyLogRepository.findByUserIdAndStudyTargetIdOrderByStudiedDateDescCreatedAtDesc(userId, studyTargetId).stream()
                .map(StudyLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyLogSummaryResponse weeklySummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<StudyLog> logs = studyLogRepository.findByUserIdAndStudiedDateBetween(userId, weekStart, weekEnd);
        int totalMinutes = logs.stream().map(StudyLog::getMinutes).reduce(0, Integer::sum);

        List<TargetStudyMinutesResponse> targetSummaries = logs.stream()
                .collect(Collectors.groupingBy(StudyLog::getStudyTargetId))
                .entrySet()
                .stream()
                .map(entry -> toTargetSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TargetStudyMinutesResponse::totalMinutes).reversed())
                .toList();

        List<FieldStudyMinutesResponse> fieldSummaries = logs.stream()
                .collect(Collectors.groupingBy(log -> normalizeField(log.getField()), Collectors.summingInt(StudyLog::getMinutes)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> new FieldStudyMinutesResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new StudyLogSummaryResponse(weekStart, weekEnd, totalMinutes, targetSummaries, fieldSummaries);
    }

    @Transactional
    public StudyLogResponse create(Long userId, StudyLogRequest request) {
        StudyTarget target = findTarget(userId, request.studyTargetId());
        StudyLog log = new StudyLog(
                userId,
                target.getId(),
                request.field(),
                request.studiedDate(),
                request.minutes(),
                request.memo()
        );
        return StudyLogResponse.from(studyLogRepository.save(log));
    }

    @Transactional
    public StudyLogResponse update(Long userId, Long id, StudyLogRequest request) {
        StudyLog log = findLog(userId, id);
        StudyTarget target = findTarget(userId, request.studyTargetId());
        log.update(target.getId(), request.field(), request.studiedDate(), request.minutes(), request.memo());
        return StudyLogResponse.from(log);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        studyLogRepository.delete(findLog(userId, id));
    }

    private TargetStudyMinutesResponse toTargetSummary(Long studyTargetId, List<StudyLog> logs) {
        int totalMinutes = logs.stream().map(StudyLog::getMinutes).reduce(0, Integer::sum);
        String targetName = logs.stream()
                .map(StudyLog::getStudyTarget)
                .filter(target -> target != null)
                .map(StudyTarget::getName)
                .findFirst()
                .orElse(null);
        return new TargetStudyMinutesResponse(studyTargetId, targetName, totalMinutes);
    }

    private String normalizeField(String field) {
        return field == null || field.isBlank() ? "未設定" : field;
    }

    private StudyTarget findTarget(Long userId, Long studyTargetId) {
        return studyTargetRepository.findByIdAndUserId(studyTargetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + studyTargetId));
    }

    private StudyLog findLog(Long userId, Long id) {
        return studyLogRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study log not found: " + id));
    }
}
