package com.liftlab.controller;

import com.liftlab.models.ActiveUsersResponse;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {


    private final DashboardService dashboardService;

    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/active-users")
    public ResponseEntity<ActiveUsersResponse> getActiveUsers() {
        final ActiveUsersResponse activeUsersResponse = this.dashboardService.getActiveUsers();
        return ResponseEntity.ok(activeUsersResponse);
    }

    @GetMapping("/page-views")
    public ResponseEntity<PageViewsResponse> getPageViews(
            @RequestParam(name = "offset", defaultValue = "5") int offset
    ) {
        final PageViewsResponse pageViewsResponse = this.dashboardService.getTopPages(offset);
        return ResponseEntity.ok(pageViewsResponse);
    }
}
