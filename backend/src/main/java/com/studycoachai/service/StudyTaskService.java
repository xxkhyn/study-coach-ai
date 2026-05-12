package com.studycoachai.service;

import java.util.List;

import com.studycoachai.dto.StudyTaskRequest;
import com.studycoachai.dto.StudyTaskResponse;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.entity.StudyTask;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.StudyTargetRepository;
import com.studycoachai.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyTaskService {
    private final StudyTaskRepository studyTaskRepository;
    private final StudyTargetRepository studyTargetRepository;

    public StudyTaskService(StudyTaskRepository studyTaskRepository, StudyTargetRepository studyTargetRepository) {
        this.studyTaskRepository = studyTaskRepository;
        this.studyTargetRepository = studyTargetRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyTaskResponse> list(Long userId) {
        return studyTaskRepository.findByUserIdOrderByCompletedAscDueDateAscCreatedAtDesc(userId).stream()
                .map(StudyTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyTaskResponse get(Long userId, Long id) {
        return StudyTaskResponse.from(findTask(userId, id));
    }

    @Transactional
    public StudyTaskResponse create(Long userId, StudyTaskRequest request) {
        StudyTarget target = findTarget(userId, request.studyTargetId());
        StudyTask task = new StudyTask(
                userId,
                target.getId(),
                request.title(),
                request.field(),
                request.plannedMinutes(),
                request.dueDate(),
                request.completed()
        );
        return StudyTaskResponse.from(studyTaskRepository.save(task));
    }

    @Transactional
    public StudyTaskResponse update(Long userId, Long id, StudyTaskRequest request) {
        StudyTask task = findTask(userId, id);
        StudyTarget target = findTarget(userId, request.studyTargetId());
        task.update(
                target.getId(),
                request.title(),
                request.field(),
                request.plannedMinutes(),
                request.dueDate(),
                request.completed()
        );
        return StudyTaskResponse.from(task);
    }

    @Transactional
    public StudyTaskResponse complete(Long userId, Long id, boolean completed) {
        StudyTask task = findTask(userId, id);
        task.setCompleted(completed);
        return StudyTaskResponse.from(task);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        studyTaskRepository.delete(findTask(userId, id));
    }

    private StudyTarget findTarget(Long userId, Long targetId) {
        return studyTargetRepository.findByIdAndUserId(targetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + targetId));
    }

    private StudyTask findTask(Long userId, Long id) {
        return studyTaskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study task not found: " + id));
    }
}
