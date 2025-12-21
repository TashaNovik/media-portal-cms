package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.model.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecommendationService.
 * Tests Redis ZSET operations for content recommendations.
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private ArticleService articleService;

    @Mock
    private VideoService videoService;

    @Mock
    private PodcastService podcastService;

    @InjectMocks
    private RecommendationService recommendationService;

    private ArticleResponse testArticle;
    private VideoResponse testVideo;
    private PodcastResponse testPodcast;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        testArticle = ArticleResponse.builder()
                .id(1L)
                .title("Test Article")
                .text("Content")
                .authorUsername("author")
                .createdAt(LocalDateTime.now())
                .isPublished(true)
                .viewCount(100L)
                .build();

        testVideo = VideoResponse.builder()
                .id(1L)
                .title("Test Video")
                .url("https://example.com/video.mp4")
                .durationSeconds(3600)
                .authorUsername("author")
                .createdAt(LocalDateTime.now())
                .isPublished(true)
                .viewCount(200L)
                .build();

        testPodcast = PodcastResponse.builder()
                .id(1L)
                .title("Test Podcast")
                .audioUrl("https://example.com/podcast.mp3")
                .description("Description")
                .authorUsername("author")
                .createdAt(LocalDateTime.now())
                .isPublished(true)
                .viewCount(150L)
                .episodes(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should get top content IDs from Redis ZSET")
    void getTopContentIds_ShouldReturnIdsFromZSet() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        topIds.add("5");
        topIds.add("3");
        topIds.add("1");
        when(zSetOperations.reverseRange(contains("ranking:hourly:article"), eq(0L), eq(9L)))
                .thenReturn(topIds);

        // When
        List<Long> result = recommendationService.getTopContentIds(ContentType.ARTICLE, 10);

        // Then
        assertEquals(3, result.size());
        assertEquals(5L, result.get(0));
        assertEquals(3L, result.get(1));
        assertEquals(1L, result.get(2));
    }

    @Test
    @DisplayName("Should fallback to daily ranking when hourly is empty")
    void getTopContentIds_WhenHourlyEmpty_ShouldFallbackToDaily() {
        // Given
        when(zSetOperations.reverseRange(contains("ranking:hourly:video"), anyLong(), anyLong()))
                .thenReturn(null);
        
        Set<Object> dailyIds = new LinkedHashSet<>();
        dailyIds.add("10");
        dailyIds.add("8");
        when(zSetOperations.reverseRange(contains("ranking:daily:video"), anyLong(), anyLong()))
                .thenReturn(dailyIds);

        // When
        List<Long> result = recommendationService.getTopContentIds(ContentType.VIDEO, 10);

        // Then
        assertEquals(2, result.size());
        assertEquals(10L, result.get(0));
        assertEquals(8L, result.get(1));
    }

    @Test
    @DisplayName("Should return empty list when no rankings exist")
    void getTopContentIds_WhenNoRankings_ShouldReturnEmptyList() {
        // Given
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);

        // When
        List<Long> result = recommendationService.getTopContentIds(ContentType.PODCAST, 10);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get top articles with content details")
    void getTopArticles_ShouldReturnArticleResponses() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        topIds.add("1");
        topIds.add("2");
        when(zSetOperations.reverseRange(contains("ranking:hourly:article"), anyLong(), anyLong()))
                .thenReturn(topIds);
        
        ArticleResponse article2 = ArticleResponse.builder()
                .id(2L)
                .title("Second Article")
                .isPublished(true)
                .build();

        when(articleService.getById(1L)).thenReturn(testArticle);
        when(articleService.getById(2L)).thenReturn(article2);

        // When
        List<ArticleResponse> result = recommendationService.getTopArticles(10);

        // Then
        assertEquals(2, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
        assertEquals("Second Article", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should fallback to published articles when no analytics data")
    void getTopArticles_WhenNoAnalytics_ShouldFallbackToPublished() {
        // Given
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);
        when(articleService.getPublished()).thenReturn(List.of(testArticle));

        // When
        List<ArticleResponse> result = recommendationService.getTopArticles(10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
        verify(articleService).getPublished();
    }

    @Test
    @DisplayName("Should get top videos with content details")
    void getTopVideos_ShouldReturnVideoResponses() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        topIds.add("1");
        when(zSetOperations.reverseRange(contains("ranking:hourly:video"), anyLong(), anyLong()))
                .thenReturn(topIds);
        when(videoService.getById(1L)).thenReturn(testVideo);

        // When
        List<VideoResponse> result = recommendationService.getTopVideos(10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Video", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should fallback to published videos when no analytics data")
    void getTopVideos_WhenNoAnalytics_ShouldFallbackToPublished() {
        // Given
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);
        when(videoService.getPublished()).thenReturn(List.of(testVideo));

        // When
        List<VideoResponse> result = recommendationService.getTopVideos(10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Video", result.get(0).getTitle());
        verify(videoService).getPublished();
    }

    @Test
    @DisplayName("Should get top podcasts with content details")
    void getTopPodcasts_ShouldReturnPodcastResponses() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        topIds.add("1");
        when(zSetOperations.reverseRange(contains("ranking:hourly:podcast"), anyLong(), anyLong()))
                .thenReturn(topIds);
        when(podcastService.getById(1L)).thenReturn(testPodcast);

        // When
        List<PodcastResponse> result = recommendationService.getTopPodcasts(10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Podcast", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should fallback to published podcasts when no analytics data")
    void getTopPodcasts_WhenNoAnalytics_ShouldFallbackToPublished() {
        // Given
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);
        when(podcastService.getPublished()).thenReturn(List.of(testPodcast));

        // When
        List<PodcastResponse> result = recommendationService.getTopPodcasts(10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Podcast", result.get(0).getTitle());
        verify(podcastService).getPublished();
    }

    @Test
    @DisplayName("Should handle exception when article not found")
    void getTopArticles_WhenArticleNotFound_ShouldSkip() {
        // Given
        Set<Object> topIds = new LinkedHashSet<>();
        topIds.add("1");
        topIds.add("999"); // Non-existent
        when(zSetOperations.reverseRange(contains("ranking:hourly:article"), anyLong(), anyLong()))
                .thenReturn(topIds);
        
        when(articleService.getById(1L)).thenReturn(testArticle);
        when(articleService.getById(999L)).thenThrow(new RuntimeException("Not found"));

        // When
        List<ArticleResponse> result = recommendationService.getTopArticles(10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should use correct limit for top content")
    void getTopContentIds_ShouldRespectLimit() {
        // Given
        when(zSetOperations.reverseRange(anyString(), eq(0L), eq(4L)))
                .thenReturn(new LinkedHashSet<>());

        // When
        recommendationService.getTopContentIds(ContentType.ARTICLE, 5);

        // Then
        verify(zSetOperations, atLeastOnce()).reverseRange(anyString(), eq(0L), eq(4L));
    }
}
