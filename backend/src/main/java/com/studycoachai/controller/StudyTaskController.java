package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.StudyTaskRequest;
import com.studycoachai.dto.StudyTaskResponse;
import com.studycoachai.service.StudyTaskService;
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
@RequestMapping("/api/study-tasks")
public class StudyTaskController {
    private static final long DEFAULT_USER_ID = 1L;

    private final StudyTaskService studyTaskService;

    public StudyTaskController(StudyTaskService studyTaskService) {
        this.studyTaskService = studyTaskService;
    }

    @GetMapping
    public List<StudyTaskResponse> list(@RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId) {
        return studyTaskService.list(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyTaskResponse create(
            @RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId,
            @Valid @RequestBody StudyTaskRequest request
    ) {
        return studyTaskService.create(userId, request);
    }

    @PutMapping("/{id}")
    public StudyTaskResponse update(
            @RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody StudyTaskRequest request
    ) {
        return studyTaskService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId, @PathVariable Long id) {
        studyTaskService.delete(userId, id);
    }
}
