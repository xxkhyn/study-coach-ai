package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.StudyLogRequest;
import com.studycoachai.dto.StudyLogResponse;
import com.studycoachai.dto.StudyLogSummaryResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.StudyLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-logs")
public class StudyLogController {
    private final StudyLogService studyLogService;

    public StudyLogController(StudyLogService studyLogService) {
        this.studyLogService = studyLogService;
    }

    @GetMapping
    public List<StudyLogResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return studyLogService.list(CurrentUser.id(jwt));
    }

    @GetMapping("/weekly-summary")
    public StudyLogSummaryResponse weeklySummary(@AuthenticationPrincipal Jwt jwt) {
        return studyLogService.weeklySummary(CurrentUser.id(jwt));
    }

    @GetMapping("/by-target/{studyTargetId}")
    public List<StudyLogResponse> listByTarget(@AuthenticationPrincipal Jwt jwt, @PathVariable Long studyTargetId) {
        return studyLogService.listByTarget(CurrentUser.id(jwt), studyTargetId);
    }

    @GetMapping("/{id}")
    public StudyLogResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return studyLogService.get(CurrentUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyLogResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody StudyLogRequest request) {
        return studyLogService.create(CurrentUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public StudyLogResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody StudyLogRequest request
    ) {
        return studyLogService.update(CurrentUser.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        studyLogService.delete(CurrentUser.id(jwt), id);
    }
}
