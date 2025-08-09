package com.liftlab.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.liftlab.config.DashboardOffsetConfig;
import com.liftlab.config.RedisKeyConfig;
import com.liftlab.models.UserDetailsResponse;
import com.liftlab.models.PageViewCount;
import com.liftlab.models.PageViewsResponse;
import com.liftlab.models.UserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Service class for all Dashboard related operations
 */
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


    /**
     * Method to get user details.
     * @return The instance of UserDetailsResponse
     */
    public UserDetailsResponse getUserDetails() {

        // First get the key for last five minutes
        final List<String> activeUsersKeys = this.getRedisKeys(this.dashboardOffsetConfig.getActiveUsersOffset(),
                this.redisKeyConfig.getActiveUsersKey());

        log.info("Fetching values for keys: {} to fetch users.", activeUsersKeys);

        final Set<String> users = this.redisTemplate.opsForSet().union(activeUsersKeys);
        final List<UserDetails> userDetails;
        if (Objects.nonNull(users)) {
            userDetails = users
                    .stream()
                    .map(this::getUserDetails)
                    .collect(ImmutableList.toImmutableList());
            log.info("Active users: {}", userDetails);
        } else {
            userDetails = ImmutableList.of();
            log.info("No active users found");
        }

        return UserDetailsResponse.builder()
                .withUserDetails(userDetails)
                .build();
    }


    /**
     * Method to the get top pages accessed by all users
     * @param offset The number of results to be returned.
     * @return The instance of PageViewsResponse
     */
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


    /**
     * Method to get the redis keys, which will be used to fetch from cache
     * @param minutesOffset The offset minutes
     * @param keyPrefix The prefix of the key
     * @implNote The keys in redis are store based on time. The time offset value is used to fetch the required values.
     *           For example, if the key minutesOffset is 3 and the keyPrefix is 'key', 3 keys will be generated with values
     *           key{current_time_in_minute-0}, key{current_time_in_minute-1}, key{current_time_in_minute-2},
     * @return List of keys to be fetched in redis.
     */
    protected List<String> getRedisKeys(final int minutesOffset, final String keyPrefix) {
        return IntStream.rangeClosed(0, minutesOffset - 1)
                .mapToObj(minute -> {
                    final String minuteKey = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(minute)
                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

                    return String.format("%s:%s", keyPrefix, minuteKey);

                }).collect(ImmutableList.toImmutableList());
    }

    /**
     * Method to get the user details from redis cache, given the user id.
     * @param userId The id of the user
     * @return The instance of  UserDetails
     */
    protected UserDetails getUserDetails(final String userId) {
        final List<String> redisUserSessionKey = this.getRedisKeys(this.dashboardOffsetConfig.getUserSessionsOffset(),
                String.format("%s:%s", this.redisKeyConfig.getUserSessionsKey(), userId));

        final Set<String> allSessions = this.redisTemplate.opsForSet().union(redisUserSessionKey);
        int totalSessions = 0;
        if(Objects.nonNull(allSessions)) {
            totalSessions = allSessions.size();
        }
        return UserDetails.builder()
                .withUserId(userId)
                .withSessionCount(totalSessions)
                .build();
    }

}
