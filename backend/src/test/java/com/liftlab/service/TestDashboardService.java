package com.liftlab.service;

import com.flextrade.jfixture.JFixture;
import com.liftlab.config.DashboardOffsetConfig;
import com.liftlab.config.RedisKeyConfig;
import com.liftlab.models.UserDetailsResponse;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.models.UserDetails;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.HashOperations;
import java.util.Set;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;


public class TestDashboardService {

    private static StringRedisTemplate redisTemplate;

    private static RedisKeyConfig redisKeyConfig;

    private static DashboardOffsetConfig dashboardOffsetConfig;

    private DashboardService dashboardService;

    @BeforeAll
    public static void setUp() {
        TestDashboardService.redisTemplate = Mockito.mock(StringRedisTemplate.class);
        final JFixture fixture = new JFixture();
        TestDashboardService.redisKeyConfig = fixture.create(RedisKeyConfig.class);
        TestDashboardService.dashboardOffsetConfig = fixture.create(DashboardOffsetConfig.class);
    }

    @BeforeEach
    public void init() {
        dashboardService = new DashboardService(
            TestDashboardService.redisTemplate,
            TestDashboardService.redisKeyConfig,
            TestDashboardService.dashboardOffsetConfig
        );

    }

    @Test
    public void testGetUserDetails() {
        SetOperations<String, String> setOps = Mockito.mock(SetOperations.class);
        Mockito.when(redisTemplate.opsForSet()).thenReturn(setOps);
        Mockito.when(setOps.members(Mockito.anyString())).thenReturn(Set.of("user1", "user2"));

        UserDetailsResponse response = dashboardService.getUserDetails();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.getUserDetails().size());
    }

    @Test
    public void testGetTopPages() {
        HashOperations<String, Object, Object> hashOps = Mockito.mock(HashOperations.class);
        Mockito.when(redisTemplate.opsForHash()).thenReturn(hashOps);
        Mockito.when(hashOps.entries(Mockito.anyString())).thenReturn(Map.of("page1", "5", "page2", "3"));

        PageViewsResponse response = dashboardService.getTopPages(2);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.getPageViews().size());
    }

    @Test
    public void testGetUserDetailsById() {
        SetOperations<String, String> setOps = Mockito.mock(SetOperations.class);
        Mockito.when(redisTemplate.opsForSet()).thenReturn(setOps);
        Mockito.when(setOps.members(Mockito.anyString())).thenReturn(Set.of("session1", "session2"));

        UserDetails userDetails = dashboardService.getUserDetails("user1");
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals(2, userDetails.getSessionCount());
    }
    public void testGetRedisKey() {
        List<String> values = dashboardService.getRedisKeys(3, "test");
        Assertions.assertEquals(3, values.size());
        values.forEach(value -> Assertions.assertTrue(value.startsWith("test")));
    }

}
