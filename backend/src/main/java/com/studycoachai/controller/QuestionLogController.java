package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.FieldAccuracyResponse;
import com.studycoachai.dto.QuestionLogRequest;
import com.studycoachai.dto.QuestionLogResponse;
import com.studycoachai.dto.WeakFieldResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.QuestionLogService;
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
@RequestMapping("/api/question-logs")
public class QuestionLogController {
    private final QuestionLogService questionLogService;

    public QuestionLogController(QuestionLogService questionLogService) {
        this.questionLogService = questionLogService;
    }

    @GetMapping
    public List<QuestionLogResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return questionLogService.list(CurrentUser.id(jwt));
    }

    @GetMapping("/accuracy-by-field")
    public List<FieldAccuracyResponse> accuracyByField(@AuthenticationPrincipal Jwt jwt) {
        return questionLogService.accuracyByField(CurrentUser.id(jwt));
    }

    @GetMapping("/weak-fields")
    public List<WeakFieldResponse> weakFields(@AuthenticationPrincipal Jwt jwt) {
        return questionLogService.weakFields(CurrentUser.id(jwt));
    }

    @GetMapping("/{id}")
    public QuestionLogResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return questionLogService.get(CurrentUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionLogResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody QuestionLogRequest request) {
        return questionLogService.create(CurrentUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public QuestionLogResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody QuestionLogRequest request
    ) {
        return questionLogService.update(CurrentUser.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        questionLogService.delete(CurrentUser.id(jwt), id);
    }
}
