package com.studycoachai.service;

import java.util.List;

import com.studycoachai.dto.StudyTargetRequest;
import com.studycoachai.dto.StudyTargetResponse;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.entity.User;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.StudyTargetRepository;
import com.studycoachai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyTargetService {
    private final StudyTargetRepository studyTargetRepository;
    private final UserRepository userRepository;

    public StudyTargetService(StudyTargetRepository studyTargetRepository, UserRepository userRepository) {
        this.studyTargetRepository = studyTargetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyTargetResponse> list(Long userId) {
        return studyTargetRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(StudyTargetResponse::from)
                .toList();
    }

    @Transactional
    public StudyTargetResponse create(Long userId, StudyTargetRequest request) {
        User user = findUser(userId);
        StudyTarget target = new StudyTarget(
                user,
                request.name(),
                request.category(),
                request.examDate(),
                request.goalDate(),
                request.memo()
        );
        return StudyTargetResponse.from(studyTargetRepository.save(target));
    }

    @Transactional
    public StudyTargetResponse update(Long userId, Long id, StudyTargetRequest request) {
        StudyTarget target = findTarget(userId, id);
        target.update(
                request.name(),
                request.category(),
                request.examDate(),
                request.goalDate(),
                request.memo()
        );
        return StudyTargetResponse.from(target);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        StudyTarget target = findTarget(userId, id);
        studyTargetRepository.delete(target);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private StudyTarget findTarget(Long userId, Long id) {
        return studyTargetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + id));
    }
}
