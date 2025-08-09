package com.liftlab.controller;

import com.google.common.collect.ImmutableList;
import com.liftlab.models.UserDetailsResponse;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Slf4j
public class DashboardController {


    private final DashboardService dashboardService;

    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Method to get the active users
     * @return Http response with user details
     */
    @GetMapping("/active-users")
    public ResponseEntity<UserDetailsResponse> getActiveUsers() {
        try {
            final UserDetailsResponse userDetailsResponse = this.dashboardService.getUserDetails();
            return ResponseEntity.ok(userDetailsResponse);
        } catch (Exception e) {
            log.error("Failed to fetch the details", e);
            return ResponseEntity.internalServerError()
                    .body(UserDetailsResponse.builder()
                            .withUserDetails(ImmutableList.of())
                            .build());
        }
    }

    /**
     * Method to the viewed pages
     * @param offset The number of results to be returned. Default will be 5
     * @return Http response with page viewed
     */
    @GetMapping("/page-views")
    public ResponseEntity<PageViewsResponse> getPageViews(
            @RequestParam(name = "offset", defaultValue = "5") int offset
    ) {
        try {
            final PageViewsResponse pageViewsResponse = this.dashboardService.getTopPages(offset);
            return ResponseEntity.ok(pageViewsResponse);
        } catch (Exception e) {
            log.error("Failed to fetch the details", e);
            return ResponseEntity.internalServerError()
                    .body(PageViewsResponse.builder()
                            .withPageViews(ImmutableList.of())
                            .build());
        }

    }
}
