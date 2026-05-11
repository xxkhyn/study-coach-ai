package com.studycoachai.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.studycoachai.dto.DashboardResponse;
import com.studycoachai.dto.StudyTaskResponse;
import com.studycoachai.entity.StudyTask;
import com.studycoachai.repository.StudyTargetRepository;
import com.studycoachai.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final StudyTargetRepository studyTargetRepository;
    private final StudyTaskRepository studyTaskRepository;

    public DashboardService(StudyTargetRepository studyTargetRepository, StudyTaskRepository studyTaskRepository) {
        this.studyTargetRepository = studyTargetRepository;
        this.studyTaskRepository = studyTaskRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<StudyTask> tasks = studyTaskRepository.findByUserIdOrderByCompletedAscDueDateAscCreatedAtDesc(userId);

        long completed = tasks.stream().filter(StudyTask::isCompleted).count();
        long overdue = tasks.stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .count();
        int plannedMinutesThisWeek = tasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> !task.getDueDate().isBefore(weekStart) && !task.getDueDate().isAfter(weekEnd))
                .map(StudyTask::getPlannedMinutes)
                .map(minutes -> minutes == null ? 0 : minutes)
                .reduce(0, Integer::sum);

        List<StudyTaskResponse> todayTasks = tasks.stream()
                .filter(task -> today.equals(task.getDueDate()))
                .map(StudyTaskResponse::from)
                .toList();

        List<StudyTaskResponse> upcomingTasks = tasks.stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDueDate() != null && !task.getDueDate().isBefore(today))
                .sorted(Comparator.comparing(StudyTask::getDueDate))
                .limit(5)
                .map(StudyTaskResponse::from)
                .toList();

        long targetCount = studyTargetRepository.findByUserIdOrderByCreatedAtDesc(userId).size();

        return new DashboardResponse(
                targetCount,
                tasks.size(),
                completed,
                tasks.size() - completed,
                overdue,
                plannedMinutesThisWeek,
                todayTasks,
                upcomingTasks
        );
    }
}
