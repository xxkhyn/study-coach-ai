package com.studycoachai.service;

import java.util.List;

import com.studycoachai.dto.StudyTargetRequest;
import com.studycoachai.dto.StudyTargetResponse;
import com.studycoachai.entity.StudyTarget;
import com.studycoachai.exception.ResourceNotFoundException;
import com.studycoachai.repository.StudyTargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyTargetService {
    private final StudyTargetRepository studyTargetRepository;

    public StudyTargetService(StudyTargetRepository studyTargetRepository) {
        this.studyTargetRepository = studyTargetRepository;
    }

    @Transactional(readOnly = true)
    public List<StudyTargetResponse> list(Long userId) {
        return studyTargetRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(StudyTargetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyTargetResponse get(Long userId, Long id) {
        return StudyTargetResponse.from(findTarget(userId, id));
    }

    @Transactional
    public StudyTargetResponse create(Long userId, StudyTargetRequest request) {
        StudyTarget target = new StudyTarget(
                userId,
                request.name(),
                request.description(),
                request.targetDate()
        );
        return StudyTargetResponse.from(studyTargetRepository.save(target));
    }

    @Transactional
    public StudyTargetResponse update(Long userId, Long id, StudyTargetRequest request) {
        StudyTarget target = findTarget(userId, id);
        target.update(request.name(), request.description(), request.targetDate());
        return StudyTargetResponse.from(target);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        studyTargetRepository.delete(findTarget(userId, id));
    }

    private StudyTarget findTarget(Long userId, Long id) {
        return studyTargetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study target not found: " + id));
    }
}
