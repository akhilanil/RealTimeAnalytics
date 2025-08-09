package com.liftlab.controller;

import com.google.common.collect.ImmutableList;
import com.liftlab.models.UserDetailsResponse;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
public class DashboardControllerTest {

    @MockBean
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    public void testGetActiveUsers_InternalServerError() throws Exception {
        when(dashboardService.getUserDetails()).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/v1/dashboard/active-users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"userDetails\":[]}"));
    }

    @Test
    public void testGetPageViews_InternalServerError() throws Exception {
        when(dashboardService.getTopPages(5)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/v1/dashboard/page-views")
                .param("offset", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"pageViews\":[]}"));
    }

    @Test
    public void testGetActiveUsers() throws Exception {
        UserDetailsResponse userDetailsResponse = UserDetailsResponse.builder().withUserDetails(ImmutableList.of()).build();
        when(dashboardService.getUserDetails()).thenReturn(userDetailsResponse);

        mockMvc.perform(get("/api/v1/dashboard/active-users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    public void testGetPageViews() throws Exception {
        PageViewsResponse pageViewsResponse = PageViewsResponse.builder().withPageViews(ImmutableList.of()).build();
        when(dashboardService.getTopPages(5)).thenReturn(pageViewsResponse);

        mockMvc.perform(get("/api/v1/dashboard/page-views")
                .param("offset", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }
}
