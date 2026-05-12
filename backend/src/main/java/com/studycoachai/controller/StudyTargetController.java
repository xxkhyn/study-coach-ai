package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.StudyTargetRequest;
import com.studycoachai.dto.StudyTargetResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.StudyTargetService;
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
@RequestMapping("/api/study-targets")
public class StudyTargetController {
    private final StudyTargetService studyTargetService;

    public StudyTargetController(StudyTargetService studyTargetService) {
        this.studyTargetService = studyTargetService;
    }

    @GetMapping
    public List<StudyTargetResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return studyTargetService.list(CurrentUser.id(jwt));
    }

    @GetMapping("/{id}")
    public StudyTargetResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return studyTargetService.get(CurrentUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyTargetResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody StudyTargetRequest request) {
        return studyTargetService.create(CurrentUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public StudyTargetResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody StudyTargetRequest request
    ) {
        return studyTargetService.update(CurrentUser.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        studyTargetService.delete(CurrentUser.id(jwt), id);
    }
}
