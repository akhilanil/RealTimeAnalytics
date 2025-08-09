package com.liftlab.service;

import com.flextrade.jfixture.JFixture;
import com.liftlab.config.DashboardOffsetConfig;
import com.liftlab.config.RedisKeyConfig;
import org.junit.jupiter.api.Assertions;
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
    public void testGetRedisKey() {
        List<String> values = dashboardService.getRedisKeys(3, "test");
        Assertions.assertEquals(3, values.size());
        values.forEach(value -> Assertions.assertTrue(value.startsWith("test")));
    }

}
