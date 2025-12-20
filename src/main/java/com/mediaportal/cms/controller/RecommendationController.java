package com.mediaportal.cms.controller;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.service.AnalyticsService;
import com.mediaportal.cms.service.ArticleService;
import com.mediaportal.cms.service.VideoService;
import com.mediaportal.cms.service.PodcastService;
import com.mediaportal.cms.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for content recommendations using Redis ZSET rankings.
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Real-time content recommendations based on view analytics")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final AnalyticsService analyticsService;
    private final ArticleService articleService;
    private final VideoService videoService;
    private final PodcastService podcastService;

    @GetMapping("/articles")
    @Operation(
        summary = "Get top articles",
        description = "Returns top 10 most viewed articles using Redis ZSET ranking"
    )
    @ApiResponse(responseCode = "200", description = "List of top articles")
    public ResponseEntity<List<ArticleResponse>> getTopArticles(
            @Parameter(description = "Maximum number of articles to return")
            @RequestParam(defaultValue = "10") int limit) {
        List<ArticleResponse> topArticles = recommendationService.getTopArticles(limit);
        return ResponseEntity.ok(topArticles);
    }

    @GetMapping("/videos")
    @Operation(
        summary = "Get top videos",
        description = "Returns top 10 most viewed videos using Redis ZSET ranking"
    )
    @ApiResponse(responseCode = "200", description = "List of top videos")
    public ResponseEntity<List<VideoResponse>> getTopVideos(
            @Parameter(description = "Maximum number of videos to return")
            @RequestParam(defaultValue = "10") int limit) {
        List<VideoResponse> topVideos = recommendationService.getTopVideos(limit);
        return ResponseEntity.ok(topVideos);
    }

    @GetMapping("/podcasts")
    @Operation(
        summary = "Get top podcasts",
        description = "Returns top 10 most viewed podcasts using Redis ZSET ranking"
    )
    @ApiResponse(responseCode = "200", description = "List of top podcasts")
    public ResponseEntity<List<PodcastResponse>> getTopPodcasts(
            @Parameter(description = "Maximum number of podcasts to return")
            @RequestParam(defaultValue = "10") int limit) {
        List<PodcastResponse> topPodcasts = recommendationService.getTopPodcasts(limit);
        return ResponseEntity.ok(topPodcasts);
    }

    @GetMapping("/all")
    @Operation(
        summary = "Get all top content",
        description = "Returns top content from all categories combined"
    )
    @ApiResponse(responseCode = "200", description = "Map with top content by category")
    public ResponseEntity<Map<String, Object>> getAllTopContent(
            @Parameter(description = "Maximum number of items per category")
            @RequestParam(defaultValue = "5") int limitPerCategory) {
        Map<String, Object> allContent = new HashMap<>();
        allContent.put("topArticles", recommendationService.getTopArticles(limitPerCategory));
        allContent.put("topVideos", recommendationService.getTopVideos(limitPerCategory));
        allContent.put("topPodcasts", recommendationService.getTopPodcasts(limitPerCategory));
        return ResponseEntity.ok(allContent);
    }
}
