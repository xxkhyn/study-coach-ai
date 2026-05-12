package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.QuestionGenerationLogResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.QuestionGenerationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/question-generation-logs")
public class QuestionGenerationLogController {
    private final QuestionGenerationService questionGenerationService;

    public QuestionGenerationLogController(QuestionGenerationService questionGenerationService) {
        this.questionGenerationService = questionGenerationService;
    }

    @GetMapping
    public List<QuestionGenerationLogResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return questionGenerationService.history(CurrentUser.id(jwt));
    }
}
