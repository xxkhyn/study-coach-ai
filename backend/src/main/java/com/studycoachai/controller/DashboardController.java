package com.studycoachai.controller;

import com.studycoachai.dto.DashboardResponse;
import com.studycoachai.security.CurrentUser;
import com.studycoachai.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.getDashboard(CurrentUser.id(jwt));
    }
}
