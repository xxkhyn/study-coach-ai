package com.studycoachai.service;

import java.util.List;

import com.studycoachai.dto.StudyTaskRequest;
import com.studycoachai.dto.StudyTaskResponse;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.entity.StudyTask;
import com.studycoachai.entity.User;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.StudyTargetRepository;
import com.studycoachai.repository.StudyTaskRepository;
import com.studycoachai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyTaskService {
    private final StudyTaskRepository studyTaskRepository;
    private final StudyTargetRepository studyTargetRepository;
    private final UserRepository userRepository;

    public StudyTaskService(
            StudyTaskRepository studyTaskRepository,
            StudyTargetRepository studyTargetRepository,
            UserRepository userRepository
    ) {
        this.studyTaskRepository = studyTaskRepository;
        this.studyTargetRepository = studyTargetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyTaskResponse> list(Long userId) {
        return studyTaskRepository.findByUserIdOrderByCompletedAscDueDateAscCreatedAtDesc(userId).stream()
                .map(StudyTaskResponse::from)
                .toList();
    }

    @Transactional
    public StudyTaskResponse create(Long userId, StudyTaskRequest request) {
        User user = findUser(userId);
        StudyTarget target = findTarget(userId, request.targetId());
        StudyTask task = new StudyTask(
                user,
                target,
                request.title(),
                request.fieldName(),
                request.plannedMinutes(),
                request.dueDate(),
                request.completed()
        );
        return StudyTaskResponse.from(studyTaskRepository.save(task));
    }

    @Transactional
    public StudyTaskResponse update(Long userId, Long id, StudyTaskRequest request) {
        StudyTask task = findTask(userId, id);
        StudyTarget target = findTarget(userId, request.targetId());
        task.update(
                target,
                request.title(),
                request.fieldName(),
                request.plannedMinutes(),
                request.dueDate(),
                request.completed()
        );
        return StudyTaskResponse.from(task);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        StudyTask task = findTask(userId, id);
        studyTaskRepository.delete(task);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
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
