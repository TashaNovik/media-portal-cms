package com.mediaportal.cms.service;

import com.mediaportal.cms.model.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnalyticsService.
 * Tests Redis operations: INCR, ZSET, SET, TTL.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Should increment view count using Redis INCR")
    void incrementViewCount_ShouldIncrementCounter() {
        // Given
        ContentType contentType = ContentType.ARTICLE;
        Long contentId = 1L;
        when(valueOperations.increment(anyString())).thenReturn(5L);
        when(redisTemplate.expire(anyString(), any())).thenReturn(true);

        // When
        analyticsService.incrementViewCount(contentType, contentId);

        // Then
        verify(valueOperations).increment(contains("views:total:article:1"));
        verify(zSetOperations).incrementScore(contains("ranking:hourly:article"), eq("1"), eq(1.0));
        verify(zSetOperations).incrementScore(contains("ranking:daily:article"), eq("1"), eq(1.0));
    }

    @Test
    @DisplayName("Should get view count from Redis")
    void getViewCount_ShouldReturnCount() {
        // Given
        ContentType contentType = ContentType.ARTICLE;
        Long contentId = 1L;
        when(valueOperations.get(anyString())).thenReturn("42");

        // When
        Long viewCount = analyticsService.getViewCount(contentType, contentId);

        // Then
        assertEquals(42L, viewCount);
        verify(valueOperations).get(contains("views:total:article:1"));
    }

    @Test
    @DisplayName("Should return 0 when view count key doesn't exist")
    void getViewCount_WhenKeyNotExists_ShouldReturnZero() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        Long viewCount = analyticsService.getViewCount(ContentType.VIDEO, 99L);

        // Then
        assertEquals(0L, viewCount);
    }

    @Test
    @DisplayName("Should get top content IDs from ZSET")
    void getTopContent_ShouldReturnTopIds() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        topIds.add("5");
        topIds.add("3");
        topIds.add("1");
        when(zSetOperations.reverseRange(anyString(), eq(0L), eq(9L))).thenReturn(topIds);

        // When
        List<Long> result = analyticsService.getTopContent(ContentType.ARTICLE, 10);

        // Then
        assertEquals(3, result.size());
        assertEquals(5L, result.get(0));
        assertEquals(3L, result.get(1));
        assertEquals(1L, result.get(2));
    }

    @Test
    @DisplayName("Should return empty list when no top content")
    void getTopContent_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);

        // When
        List<Long> result = analyticsService.getTopContent(ContentType.VIDEO, 10);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should track unique visitor using Redis SET")
    void trackUniqueVisitor_NewVisitor_ShouldReturnTrue() {
        // Given
        when(setOperations.add(anyString(), anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any())).thenReturn(true);

        // When
        boolean isNew = analyticsService.trackUniqueVisitor(ContentType.ARTICLE, 1L, "visitor123");

        // Then
        assertTrue(isNew);
        verify(setOperations).add(contains("visitors:daily:article:1"), eq("visitor123"));
    }

    @Test
    @DisplayName("Should return false for returning visitor")
    void trackUniqueVisitor_ReturningVisitor_ShouldReturnFalse() {
        // Given
        when(setOperations.add(anyString(), anyString())).thenReturn(0L);
        when(redisTemplate.expire(anyString(), any())).thenReturn(true);

        // When
        boolean isNew = analyticsService.trackUniqueVisitor(ContentType.ARTICLE, 1L, "visitor123");

        // Then
        assertFalse(isNew);
    }

    @Test
    @DisplayName("Should get unique visitors count from SET")
    void getUniqueVisitorsCount_ShouldReturnCount() {
        // Given
        when(setOperations.size(anyString())).thenReturn(25L);

        // When
        Long count = analyticsService.getUniqueVisitorsCount(ContentType.ARTICLE, 1L);

        // Then
        assertEquals(25L, count);
    }

    @Test
    @DisplayName("Should get content analytics summary")
    void getContentAnalytics_ShouldReturnAnalyticsSummary() {
        // Given
        when(valueOperations.get(anyString())).thenReturn("100");
        when(setOperations.size(anyString())).thenReturn(50L);
        when(zSetOperations.reverseRank(anyString(), anyString())).thenReturn(0L);

        // When
        Map<String, Object> analytics = analyticsService.getContentAnalytics(ContentType.ARTICLE, 1L);

        // Then
        assertEquals(ContentType.ARTICLE.name(), analytics.get("contentType"));
        assertEquals(1L, analytics.get("contentId"));
        assertEquals(100L, analytics.get("totalViews"));
        assertEquals(50L, analytics.get("uniqueVisitorsToday"));
        assertEquals(1L, analytics.get("currentRank"));
    }

    @Test
    @DisplayName("Should clear analytics data")
    void clearAnalytics_ShouldDeleteRedisKeys() {
        // Given
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // When
        analyticsService.clearAnalytics(ContentType.ARTICLE, 1L);

        // Then
        verify(redisTemplate).delete(contains("views:total:article:1"));
        verify(zSetOperations).remove(contains("ranking:hourly:article"), eq("1"));
        verify(zSetOperations).remove(contains("ranking:daily:article"), eq("1"));
    }

    @Test
    @DisplayName("Should get top articles convenience method")
    void getTopArticles_ShouldReturn10Articles() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        for (int i = 1; i <= 10; i++) {
            topIds.add(String.valueOf(i));
        }
        when(zSetOperations.reverseRange(anyString(), eq(0L), eq(9L))).thenReturn(topIds);

        // When
        List<Long> result = analyticsService.getTopArticles();

        // Then
        assertEquals(10, result.size());
    }

    @Test
    @DisplayName("Should get platform analytics")
    void getPlatformAnalytics_ShouldReturnSummary() {
        // Given
        when(zSetOperations.size(anyString())).thenReturn(5L);

        // When
        Map<String, Object> analytics = analyticsService.getPlatformAnalytics();

        // Then
        assertNotNull(analytics.get("generatedAt"));
        assertEquals(5L, analytics.get("articleInRanking"));
        assertEquals(5L, analytics.get("videoInRanking"));
        assertEquals(5L, analytics.get("podcastInRanking"));
    }
}
