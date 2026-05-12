package com.studycoachai.controller;

import java.util.List;

import com.studycoachai.dto.DailyStudyTimeResponse;
import com.studycoachai.dto.FieldAccuracyResponse;
import com.studycoachai.dto.TargetStudyMinutesResponse;
import com.studycoachai.dto.WeakFieldResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.AnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/study-time/daily")
    public List<DailyStudyTimeResponse> dailyStudyTime(@AuthenticationPrincipal Jwt jwt) {
        return analyticsService.dailyStudyTime(CurrentUser.id(jwt));
    }

    @GetMapping("/study-time/by-target")
    public List<TargetStudyMinutesResponse> studyTimeByTarget(@AuthenticationPrincipal Jwt jwt) {
        return analyticsService.studyTimeByTarget(CurrentUser.id(jwt));
    }

    @GetMapping("/accuracy/by-field")
    public List<FieldAccuracyResponse> accuracyByField(@AuthenticationPrincipal Jwt jwt) {
        return analyticsService.accuracyByField(CurrentUser.id(jwt));
    }

    @GetMapping("/weak-fields")
    public List<WeakFieldResponse> weakFields(@AuthenticationPrincipal Jwt jwt) {
        return analyticsService.weakFields(CurrentUser.id(jwt));
    }
}
