package com.liftlab.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RedisKeyConfig {

    @Value("${dashboard.redis.keys.active-users}")
    private String activeUsersKey;

    @Value("${dashboard.redis.keys.page-views}")
    private String pageViewsKey;

    @Value("${dashboard.redis.keys.user-sessions}")
    private String userSessionsKey;
}
