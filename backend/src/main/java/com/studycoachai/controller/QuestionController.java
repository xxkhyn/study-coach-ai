package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.QuestionAnswerRequest;
import com.studycoachai.dto.QuestionAnswerResponse;
import com.studycoachai.dto.QuestionGenerationRequest;
import com.studycoachai.dto.QuestionImportResultResponse;
import com.studycoachai.dto.QuestionRequest;
import com.studycoachai.dto.QuestionResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.QuestionGenerationService;
import com.studycoachai.service.QuestionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionGenerationService questionGenerationService;

    public QuestionController(QuestionService questionService, QuestionGenerationService questionGenerationService) {
        this.questionService = questionService;
        this.questionGenerationService = questionGenerationService;
    }

    @GetMapping
    public List<QuestionResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return questionService.list(CurrentUser.id(jwt));
    }

    @GetMapping("/random")
    public QuestionResponse random(@AuthenticationPrincipal Jwt jwt) {
        return questionService.random(CurrentUser.id(jwt));
    }

    @GetMapping("/wrong")
    public List<QuestionResponse> wrong(@AuthenticationPrincipal Jwt jwt) {
        return questionService.wrongQuestions(CurrentUser.id(jwt));
    }

    @GetMapping("/{id}")
    public QuestionResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return questionService.get(CurrentUser.id(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody QuestionRequest request) {
        return questionService.create(CurrentUser.id(jwt), request);
    }

    @PostMapping("/import-csv")
    public QuestionImportResultResponse importCsv(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long studyTargetId,
            @RequestParam("file") MultipartFile file
    ) {
        return questionService.importCsv(CurrentUser.id(jwt), studyTargetId, file);
    }

    @PostMapping("/generate-ai")
    @ResponseStatus(HttpStatus.CREATED)
    public List<QuestionResponse> generateAi(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody QuestionGenerationRequest request
    ) {
        return questionGenerationService.generate(CurrentUser.id(jwt), request);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request
    ) {
        return questionService.update(CurrentUser.id(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        questionService.delete(CurrentUser.id(jwt), id);
    }

    @PostMapping("/{id}/answer")
    public QuestionAnswerResponse answer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody QuestionAnswerRequest request
    ) {
        return questionService.answer(CurrentUser.id(jwt), id, request);
    }
}
