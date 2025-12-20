package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.model.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for content recommendations using Redis Sorted Sets (ZSET).
 * Implements real-time ranking based on view counts.
 * 
 * Redis structures used:
 * - ZSET for rankings (views:hourly:{contentType}, views:daily:{contentType})
 * - String for view counters (views:{contentType}:{id})
 * - SET for unique visitors tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleService articleService;
    private final VideoService videoService;
    private final PodcastService podcastService;

    private static final String HOURLY_RANKING_KEY = "ranking:hourly:%s";
    private static final String DAILY_RANKING_KEY = "ranking:daily:%s";
    private static final String UNIQUE_VISITORS_KEY = "visitors:%s:%s:%d";
    private static final int DEFAULT_TOP_COUNT = 10;

    /**
     * Get top N most viewed articles in the last hour using ZSET.
     * Uses @Cacheable for additional caching layer with 5 min TTL.
     */
    @Cacheable(value = "topContent", key = "'topArticles:' + #limit")
    public List<ArticleResponse> getTopArticles(int limit) {
        List<Long> topIds = getTopContentIds(ContentType.ARTICLE, limit);
        
        if (topIds.isEmpty()) {
            // Fallback: return latest articles if no analytics data
            return articleService.getPublished().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return topIds.stream()
                .map(id -> {
                    try {
                        return articleService.getById(id);
                    } catch (Exception e) {
                        log.warn("Article not found: {}", id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get top N most viewed videos in the last hour.
     */
    @Cacheable(value = "topContent", key = "'topVideos:' + #limit")
    public List<VideoResponse> getTopVideos(int limit) {
        List<Long> topIds = getTopContentIds(ContentType.VIDEO, limit);
        
        if (topIds.isEmpty()) {
            return videoService.getPublished().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return topIds.stream()
                .map(id -> {
                    try {
                        return videoService.getById(id);
                    } catch (Exception e) {
                        log.warn("Video not found: {}", id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get top N most viewed podcasts in the last hour.
     */
    @Cacheable(value = "topContent", key = "'topPodcasts:' + #limit")
    public List<PodcastResponse> getTopPodcasts(int limit) {
        List<Long> topIds = getTopContentIds(ContentType.PODCAST, limit);
        
        if (topIds.isEmpty()) {
            return podcastService.getPublished().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return topIds.stream()
                .map(id -> {
                    try {
                        return podcastService.getById(id);
                    } catch (Exception e) {
                        log.warn("Podcast not found: {}", id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get top content IDs from Redis ZSET (Sorted Set).
     * Uses ZREVRANGE to get items with highest scores first.
     */
    public List<Long> getTopContentIds(ContentType contentType, int limit) {
        String hourlyKey = String.format(HOURLY_RANKING_KEY, contentType.name().toLowerCase());
        
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<Object> topIds = zSetOps.reverseRange(hourlyKey, 0, limit - 1);
        
        if (topIds == null || topIds.isEmpty()) {
            // Try daily ranking as fallback
            String dailyKey = String.format(DAILY_RANKING_KEY, contentType.name().toLowerCase());
            topIds = zSetOps.reverseRange(dailyKey, 0, limit - 1);
        }
        
        if (topIds == null || topIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return topIds.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());
    }

    /**
     * Get score (view count) for specific content from ZSET.
     */
    public Double getContentScore(ContentType contentType, Long contentId) {
        String hourlyKey = String.format(HOURLY_RANKING_KEY, contentType.name().toLowerCase());
        Double score = redisTemplate.opsForZSet().score(hourlyKey, contentId.toString());
        return score != null ? score : 0.0;
    }

    /**
     * Get ranking position for specific content.
     */
    public Long getContentRank(ContentType contentType, Long contentId) {
        String hourlyKey = String.format(HOURLY_RANKING_KEY, contentType.name().toLowerCase());
        Long rank = redisTemplate.opsForZSet().reverseRank(hourlyKey, contentId.toString());
        return rank != null ? rank + 1 : null; // Convert to 1-based ranking
    }

    /**
     * Track unique visitor for content using Redis SET.
     * Returns true if this is a new unique visitor.
     */
    public boolean trackUniqueVisitor(ContentType contentType, Long contentId, String visitorId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = String.format(UNIQUE_VISITORS_KEY, today, contentType.name().toLowerCase(), contentId);
        
        Long added = redisTemplate.opsForSet().add(key, visitorId);
        
        // Set TTL of 24 hours for unique visitors tracking
        redisTemplate.expire(key, Duration.ofHours(24));
        
        return added != null && added > 0;
    }

    /**
     * Get unique visitors count for content.
     */
    public Long getUniqueVisitorsCount(ContentType contentType, Long contentId) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = String.format(UNIQUE_VISITORS_KEY, today, contentType.name().toLowerCase(), contentId);
        
        Long count = redisTemplate.opsForSet().size(key);
        return count != null ? count : 0L;
    }

    /**
     * Get combined recommendations across all content types.
     */
    public Map<String, Object> getAllTopContent(int limit) {
        Map<String, Object> recommendations = new HashMap<>();
        recommendations.put("topArticles", getTopArticles(limit));
        recommendations.put("topVideos", getTopVideos(limit));
        recommendations.put("topPodcasts", getTopPodcasts(limit));
        recommendations.put("generatedAt", LocalDateTime.now());
        return recommendations;
    }

    /**
     * Clear hourly rankings (can be scheduled).
     */
    public void clearHourlyRankings() {
        for (ContentType type : ContentType.values()) {
            String key = String.format(HOURLY_RANKING_KEY, type.name().toLowerCase());
            redisTemplate.delete(key);
        }
        log.info("Cleared hourly rankings");
    }
}
