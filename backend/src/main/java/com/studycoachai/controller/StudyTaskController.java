package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.StudyTaskRequest;
import com.studycoachai.dto.StudyTaskResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.StudyTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final StudyTaskService studyTaskService;

    public StudyTaskController(StudyTaskService studyTaskService) {
        this.studyTaskService = studyTaskService;
    }

    @GetMapping
    public List<StudyTaskResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return studyTaskService.list(CurrentUser.id(jwt));
    }

    @GetMapping("/{id}")
    public StudyTaskResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return studyTaskService.get(CurrentUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyTaskResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody StudyTaskRequest request) {
        return studyTaskService.create(CurrentUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public StudyTaskResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody StudyTaskRequest request
    ) {
        return studyTaskService.update(CurrentUser.id(jwt), id, request);
    }

    @PatchMapping("/{id}/complete")
    public StudyTaskResponse complete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean completed
    ) {
        return studyTaskService.complete(CurrentUser.id(jwt), id, completed);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        studyTaskService.delete(CurrentUser.id(jwt), id);
    }
}
