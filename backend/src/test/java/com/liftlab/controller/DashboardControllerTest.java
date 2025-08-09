package com.liftlab.controller;

import com.google.common.collect.ImmutableList;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.models.UserDetailsResponse;
import com.liftlab.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link DashboardController}.
 */
@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Nested
    @DisplayName("GET /api/v1/dashboard/active-users")
    class ActiveUsers {

        @Test
        @DisplayName("returns 200 with body from service")
        void activeUsers_ok() throws Exception {
            // given
            UserDetailsResponse body = UserDetailsResponse.builder()
                    .withUserDetails(ImmutableList.of()) // keep simple; we just assert field presence
                    .build();
            when(dashboardService.getUserDetails()).thenReturn(body);

            // when/then
            mockMvc.perform(get("/api/v1/dashboard/active-users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userDetails", isA(Iterable.class)));
        }

        @Test
        @DisplayName("returns 500 with empty list on exception")
        void activeUsers_error() throws Exception {
            // given
            when(dashboardService.getUserDetails()).thenThrow(new RuntimeException("boom"));

            // when/then
            mockMvc.perform(get("/api/v1/dashboard/active-users"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userDetails", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/dashboard/page-views")
    class PageViews {

        @Test
        @DisplayName("uses default offset=5 and returns 200")
        void pageViews_defaultOffset_ok() throws Exception {
            // given
            PageViewsResponse body = PageViewsResponse.builder()
                    .withPageViews(ImmutableList.of())
                    .build();
            when(dashboardService.getTopPages(eq(5))).thenReturn(body);

            // when/then
            mockMvc.perform(get("/api/v1/dashboard/page-views"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.pageViews", isA(Iterable.class)));

            // also verify the default offset was used
            Mockito.verify(dashboardService).getTopPages(5);
        }

        @Test
        @DisplayName("passes provided offset to service and returns 200")
        void pageViews_customOffset_ok() throws Exception {
            // given
            int offset = 10;
            PageViewsResponse body = PageViewsResponse.builder()
                    .withPageViews(ImmutableList.of())
                    .build();
            when(dashboardService.getTopPages(eq(offset))).thenReturn(body);

            // when/then
            mockMvc.perform(get("/api/v1/dashboard/page-views").param("offset", String.valueOf(offset)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.pageViews", isA(Iterable.class)));

            Mockito.verify(dashboardService).getTopPages(offset);
        }

        @Test
        @DisplayName("returns 500 with empty list on exception")
        void pageViews_error() throws Exception {
            // given
            when(dashboardService.getTopPages(Mockito.anyInt())).thenThrow(new RuntimeException("boom"));

            // when/then
            mockMvc.perform(get("/api/v1/dashboard/page-views").param("offset", "7"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.pageViews", hasSize(0)));
        }
    }
}
