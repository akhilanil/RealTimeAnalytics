package com.liftlab;

import com.liftlab.config.DashboardOffsetConfig;
import com.liftlab.config.RedisKeyConfig;
import org.springframework.test.util.ReflectionTestUtils;

public class TestUtils {


    public static RedisKeyConfig getRedisKeyConfig() {
        final RedisKeyConfig redisKeyConfig = new RedisKeyConfig();
        ReflectionTestUtils.setField(redisKeyConfig, "activeUsersKey", "active_users");
        ReflectionTestUtils.setField(redisKeyConfig, "pageViewsKey", "page_views");
        ReflectionTestUtils.setField(redisKeyConfig, "userSessionsKey", "user_sessions");
        return redisKeyConfig;
    }


    public static DashboardOffsetConfig getDashboardOffsetKeyConfig() {
        final DashboardOffsetConfig dashboardOffsetConfig = new DashboardOffsetConfig();
        ReflectionTestUtils.setField(dashboardOffsetConfig, "activeUsersOffset", 5);
        ReflectionTestUtils.setField(dashboardOffsetConfig, "pageViewsOffset", 15);
        ReflectionTestUtils.setField(dashboardOffsetConfig, "userSessionsOffset", 5);
        return dashboardOffsetConfig;
    }

}
