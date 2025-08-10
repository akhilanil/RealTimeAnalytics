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
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;
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
     * @implNote : All the page views are stored in sorted set with count being the score. This method reads the ordered
     * set unions it based on the score and store in another ordered set with a TTL of 10s. From this new set top N(offset)
     * is picked.
     * @return The instance of PageViewsResponse
     */
    public PageViewsResponse getTopPages(final int offset) {

        // First get the key
        final List<String> keys = this.getRedisKeys(
            this.dashboardOffsetConfig.getPageViewsOffset(),
            this.redisKeyConfig.getPageViewsKey()
        );

        log.info("Fetching values for keys: {} to fetch urls.", keys);

        // Temporary key to store the union
        final String destKey = "tmp:page_views:rolling:" +
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                ":" + UUID.randomUUID();

        // Now combine all sorted set into one.
        final String first = keys.get(0);
        final Collection<String> rest = keys.subList(1, keys.size());
        Long unionCount = redisTemplate.opsForZSet().unionAndStore(first, rest, destKey);

        if (unionCount == null || unionCount == 0L) {
            redisTemplate.delete(destKey);
            return PageViewsResponse.builder().withPageViews(ImmutableList.of()).build();
        }

        // Expiry as this is short-lived.
        redisTemplate.expire(destKey, Duration.ofSeconds(10));

        // Fetch the top elements
        final Set<ZSetOperations.TypedTuple<String>> top =
                redisTemplate.opsForZSet().reverseRangeWithScores(destKey, 0, Math.max(0, offset - 1));

        if (top == null || top.isEmpty()) {
            redisTemplate.delete(destKey);
            return PageViewsResponse.builder().withPageViews(ImmutableList.of()).build();
        }


        final List<PageViewCount> pageViews = top.stream()
                .map(t -> PageViewCount.builder()
                        .withPageUrl(t.getValue())
                        .withCount(t.getScore() == null ? 0 : t.getScore().intValue())
                        .build())
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
