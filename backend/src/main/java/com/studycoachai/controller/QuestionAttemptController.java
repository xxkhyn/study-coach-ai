package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.FieldAccuracyResponse;
import com.studycoachai.dto.QuestionAttemptResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.QuestionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/question-attempts")
public class QuestionAttemptController {
    private final QuestionService questionService;

    public QuestionAttemptController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionAttemptResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return questionService.attempts(CurrentUser.id(jwt));
    }

    @GetMapping("/accuracy-by-field")
    public List<FieldAccuracyResponse> accuracyByField(@AuthenticationPrincipal Jwt jwt) {
        return questionService.attemptAccuracyByField(CurrentUser.id(jwt));
    }
}
