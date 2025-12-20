package com.mediaportal.cms.service;

import com.mediaportal.cms.model.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for real-time analytics using Redis.
 * 
 * Implements:
 * - INCR: Atomic counters for view counts
 * - ZSET (Sorted Sets): Rankings for top content
 * - TTL: Time-to-live for temporary data
 * - Sets: Unique visitors tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis key patterns
    private static final String VIEW_COUNT_KEY = "views:total:%s:%d";
    private static final String HOURLY_RANKING_KEY = "ranking:hourly:%s";
    private static final String DAILY_RANKING_KEY = "ranking:daily:%s";
    private static final String HOURLY_VIEWS_KEY = "views:hourly:%s:%s";
    private static final String UNIQUE_VISITORS_KEY = "visitors:daily:%s:%d";

    // TTL durations
    private static final Duration HOURLY_TTL = Duration.ofHours(1);
    private static final Duration DAILY_TTL = Duration.ofHours(24);

    /**
     * Increment view count for content using Redis INCR (atomic operation).
     * Also updates ZSET rankings for recommendations.
     */
    public void incrementViewCount(ContentType contentType, Long contentId) {
        String type = contentType.name().toLowerCase();
        
        // 1. Increment total view count using INCR (atomic)
        String totalKey = String.format(VIEW_COUNT_KEY, type, contentId);
        Long totalViews = redisTemplate.opsForValue().increment(totalKey);
        log.debug("Total views for {} {}: {}", contentType, contentId, totalViews);

        // 2. Increment hourly ranking in ZSET
        String hourlyRankingKey = String.format(HOURLY_RANKING_KEY, type);
        redisTemplate.opsForZSet().incrementScore(hourlyRankingKey, contentId.toString(), 1);
        redisTemplate.expire(hourlyRankingKey, HOURLY_TTL);

        // 3. Increment daily ranking in ZSET
        String dailyRankingKey = String.format(DAILY_RANKING_KEY, type);
        redisTemplate.opsForZSet().incrementScore(dailyRankingKey, contentId.toString(), 1);
        redisTemplate.expire(dailyRankingKey, DAILY_TTL);

        // 4. Track hourly views with timestamp key
        String hour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        String hourlyViewsKey = String.format(HOURLY_VIEWS_KEY, type, hour);
        redisTemplate.opsForValue().increment(hourlyViewsKey);
        redisTemplate.expire(hourlyViewsKey, HOURLY_TTL);

        log.debug("Incremented view count for {} with id {}", contentType, contentId);
    }

    /**
     * Get total view count for specific content.
     */
    public Long getViewCount(ContentType contentType, Long contentId) {
        String key = String.format(VIEW_COUNT_KEY, contentType.name().toLowerCase(), contentId);
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return 0L;
        }
        
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Get top N most viewed content IDs using Redis ZREVRANGE.
     * Returns content IDs sorted by view count (highest first).
     */
    public List<Long> getTopContent(ContentType contentType, int limit) {
        String hourlyKey = String.format(HOURLY_RANKING_KEY, contentType.name().toLowerCase());
        
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<Object> topIds = zSetOps.reverseRange(hourlyKey, 0, limit - 1);
        
        if (topIds == null || topIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return topIds.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());
    }

    /**
     * Get top 10 most viewed articles (convenience method).
     */
    public List<Long> getTopArticles() {
        return getTopContent(ContentType.ARTICLE, 10);
    }

    /**
     * Get top 10 most viewed videos.
     */
    public List<Long> getTopVideos() {
        return getTopContent(ContentType.VIDEO, 10);
    }

    /**
     * Get top 10 most viewed podcasts.
     */
    public List<Long> getTopPodcasts() {
        return getTopContent(ContentType.PODCAST, 10);
    }

    /**
     * Track unique visitor for content using Redis SET.
     * Returns true if visitor is new (not seen before today).
     */
    public boolean trackUniqueVisitor(ContentType contentType, Long contentId, String visitorId) {
        String key = String.format(UNIQUE_VISITORS_KEY, contentType.name().toLowerCase(), contentId);
        
        Long added = redisTemplate.opsForSet().add(key, visitorId);
        redisTemplate.expire(key, DAILY_TTL);
        
        boolean isNew = added != null && added > 0;
        if (isNew) {
            log.debug("New unique visitor {} for {} {}", visitorId, contentType, contentId);
        }
        
        return isNew;
    }

    /**
     * Get count of unique visitors for content today.
     */
    public Long getUniqueVisitorsCount(ContentType contentType, Long contentId) {
        String key = String.format(UNIQUE_VISITORS_KEY, contentType.name().toLowerCase(), contentId);
        Long count = redisTemplate.opsForSet().size(key);
        return count != null ? count : 0L;
    }

    /**
     * Get analytics summary for specific content.
     */
    public Map<String, Object> getContentAnalytics(ContentType contentType, Long contentId) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("contentType", contentType.name());
        analytics.put("contentId", contentId);
        analytics.put("totalViews", getViewCount(contentType, contentId));
        analytics.put("uniqueVisitorsToday", getUniqueVisitorsCount(contentType, contentId));
        
        // Get ranking position
        String hourlyKey = String.format(HOURLY_RANKING_KEY, contentType.name().toLowerCase());
        Long rank = redisTemplate.opsForZSet().reverseRank(hourlyKey, contentId.toString());
        analytics.put("currentRank", rank != null ? rank + 1 : null);
        
        return analytics;
    }

    /**
     * Get platform-wide analytics summary.
     */
    public Map<String, Object> getPlatformAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        for (ContentType type : ContentType.values()) {
            String typeLower = type.name().toLowerCase();
            String hourlyKey = String.format(HOURLY_RANKING_KEY, typeLower);
            
            Long totalInRanking = redisTemplate.opsForZSet().size(hourlyKey);
            analytics.put(typeLower + "InRanking", totalInRanking != null ? totalInRanking : 0);
        }
        
        analytics.put("generatedAt", LocalDateTime.now());
        return analytics;
    }

    /**
     * Clear analytics data for testing.
     */
    public void clearAnalytics(ContentType contentType, Long contentId) {
        String type = contentType.name().toLowerCase();
        
        String totalKey = String.format(VIEW_COUNT_KEY, type, contentId);
        redisTemplate.delete(totalKey);
        
        String hourlyKey = String.format(HOURLY_RANKING_KEY, type);
        redisTemplate.opsForZSet().remove(hourlyKey, contentId.toString());
        
        String dailyKey = String.format(DAILY_RANKING_KEY, type);
        redisTemplate.opsForZSet().remove(dailyKey, contentId.toString());
        
        log.info("Cleared analytics for {} {}", contentType, contentId);
    }
}
