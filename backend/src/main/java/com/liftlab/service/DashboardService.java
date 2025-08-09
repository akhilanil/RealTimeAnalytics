package com.liftlab.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.liftlab.config.DashboardOffsetConfig;
import com.liftlab.config.RedisKeyConfig;
import com.liftlab.models.ActiveUsersResponse;
import com.liftlab.models.PageViewCount;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.models.UserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class DashboardService {

    private final StringRedisTemplate redisTemplate;

    private final RedisKeyConfig redisKeyConfig;

    private final DashboardOffsetConfig dashboardOffsetConfig;

    public DashboardService(final StringRedisTemplate redisTemplate,
                            final RedisKeyConfig redisKeyConfig,
                            final DashboardOffsetConfig dashboardOffsetConfig) {
        this.redisTemplate = redisTemplate;
        this.redisKeyConfig = redisKeyConfig;
        this.dashboardOffsetConfig = dashboardOffsetConfig;
    }

    protected UserDetails getUserDetails(final String userId) {
        final List<String> redisUserSessionKey = this.getRedisKeys(this.dashboardOffsetConfig.getUserSessionsOffset(),
                String.format("%s:%s", this.redisKeyConfig.getUserSessionsKey(), userId));

        final int totalSessions = redisUserSessionKey.stream()
                .map(redisKeys -> this.redisTemplate.opsForSet().members(redisKeys))
                .filter(Objects::nonNull)
                .mapToInt(Set::size)
                .sum();
        return UserDetails.builder()
                .withUserId(userId)
                .withSessionCount(totalSessions)
                .build();
    }

    public ActiveUsersResponse getActiveUsers() {

        // First get the key for last five minutes
        final List<String> activeUsersKeys = this.getRedisKeys(this.dashboardOffsetConfig.getActiveUsersOffset(),
                this.redisKeyConfig.getActiveUsersKey());


        log.info("Fetching values for keys: {} to fetch users.", activeUsersKeys);

        final Set<String> users = activeUsersKeys.stream()
                .map(redisKeys -> this.redisTemplate.opsForSet().members(redisKeys))
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .collect(ImmutableSet.toImmutableSet());



        final List<UserDetails> userDetails = users
                .stream()
                .map(this::getUserDetails)
                .collect(ImmutableList.toImmutableList());

        log.info("Active users: {}", userDetails);

        return ActiveUsersResponse.builder()
                .withUserDetails(userDetails)
                .build();
    }


    public PageViewsResponse getTopPages(final int offset) {

        // First get the key for last five minutes
        final List<String> keys = this.getRedisKeys(
                this.dashboardOffsetConfig.getPageViewsOffset(),
                this.redisKeyConfig.getPageViewsKey()
        );

        log.info("Fetching values for keys: {} to fetch urls.", keys);

        final Map<String, Integer> pageMap = new HashMap<>();

        keys.forEach(redisKey -> this.redisTemplate.opsForHash()
                .entries(redisKey)
                .forEach((key, value) -> {
                    final String pageUrl = key.toString();
                    final int count = Integer.parseInt(value.toString());
                    // Add the url if absent
                    if (pageMap.containsKey(pageUrl)) {
                        pageMap.put(pageUrl, count + pageMap.get(pageUrl));
                    } else {
                        pageMap.put(pageUrl, count);
                    }

                })
        );

        final List<PageViewCount> pageViews = pageMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(offset)
                .map(entry -> PageViewCount.builder().withPageUrl(entry.getKey()).withCount(entry.getValue()).build())
                .collect(ImmutableList.toImmutableList());

        log.info("Fetched top {} pages viewed: {}", offset, pageViews);

        return PageViewsResponse.builder().withPageViews(pageViews).build();
    }


    protected List<String> getRedisKeys(final int minutesOffset, final String keyPrefix) {
        return IntStream.rangeClosed(0, minutesOffset - 1)
                .mapToObj(minute -> {
                    final String minuteKey = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(minute)
                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

                    return String.format("%s:%s", keyPrefix, minuteKey);

                }).collect(ImmutableList.toImmutableList());
    }

}
