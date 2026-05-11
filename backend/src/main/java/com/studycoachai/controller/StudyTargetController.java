package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.StudyTargetRequest;
import com.studycoachai.dto.StudyTargetResponse;
import com.studycoachai.service.StudyTargetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-targets")
public class StudyTargetController {
    private static final long DEFAULT_USER_ID = 1L;

    private final StudyTargetService studyTargetService;

    public StudyTargetController(StudyTargetService studyTargetService) {
        this.studyTargetService = studyTargetService;
    }

    @GetMapping
    public List<StudyTargetResponse> list(@RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId) {
        return studyTargetService.list(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyTargetResponse create(
            @RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId,
            @Valid @RequestBody StudyTargetRequest request
    ) {
        return studyTargetService.create(userId, request);
    }

    @PutMapping("/{id}")
    public StudyTargetResponse update(
            @RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody StudyTargetRequest request
    ) {
        return studyTargetService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId, @PathVariable Long id) {
        studyTargetService.delete(userId, id);
    }
}
