package com.studycoachai.controller;

import com.studycoachai.dto.DashboardResponse;
import com.studycoachai.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final long DEFAULT_USER_ID = 1L;

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@RequestParam(defaultValue = "" + DEFAULT_USER_ID) Long userId) {
        return dashboardService.getDashboard(userId);
    }
}
