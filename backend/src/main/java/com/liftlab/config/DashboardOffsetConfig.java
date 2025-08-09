package com.liftlab.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class DashboardOffsetConfig {

    @Value("${dashboard.config.offset.active-users}")
    private int activeUsersOffset;

    @Value("${dashboard.config.offset.page-views}")
    private int pageViewsOffset;

    @Value("${dashboard.config.offset.user-sessions}")
    private int userSessionsOffset;
}
