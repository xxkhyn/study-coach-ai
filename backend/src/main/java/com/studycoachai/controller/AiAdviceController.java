package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.AiAdviceRequest;
import com.studycoachai.dto.AiAdviceResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.AiAdviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/advice")
public class AiAdviceController {
    private final AiAdviceService aiAdviceService;

    public AiAdviceController(AiAdviceService aiAdviceService) {
        this.aiAdviceService = aiAdviceService;
    }

    @PostMapping("/daily")
    public AiAdviceResponse generateDailyAdvice(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody(required = false) AiAdviceRequest request
    ) {
        return aiAdviceService.generateDailyAdvice(CurrentUser.id(jwt), request);
    }

    @GetMapping("/today")
    public ResponseEntity<AiAdviceResponse> getTodayAdvice(@AuthenticationPrincipal Jwt jwt) {
        AiAdviceResponse response = aiAdviceService.getTodayAdvice(CurrentUser.id(jwt));
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public List<AiAdviceResponse> history(@AuthenticationPrincipal Jwt jwt) {
        return aiAdviceService.history(CurrentUser.id(jwt));
    }
}
