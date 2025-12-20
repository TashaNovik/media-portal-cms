package com.mediaportal.cms.controller;

import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Controller for analytics and view tracking using Redis.
 * 
 * Demonstrates:
 * - INCR: Atomic view count increments
 * - ZSET: Real-time rankings
 * - SET: Unique visitor tracking
 * - TTL: Automatic data expiration
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Content analytics and view tracking using Redis")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/view/{contentType}/{contentId}")
    @Operation(
        summary = "Track content view",
        description = "Increments view count using Redis INCR and updates ZSET rankings"
    )
    @ApiResponse(responseCode = "200", description = "View tracked successfully")
    public ResponseEntity<Map<String, Object>> trackView(
            @Parameter(description = "Content type", example = "ARTICLE")
            @PathVariable ContentType contentType,
            @Parameter(description = "Content ID")
            @PathVariable Long contentId,
            HttpServletRequest request) {
        
        // Get visitor identifier (IP + User-Agent hash or session)
        String visitorId = getVisitorId(request);
        
        // Track view (INCR)
        analyticsService.incrementViewCount(contentType, contentId);
        
        // Track unique visitor (SET)
        boolean isNewVisitor = analyticsService.trackUniqueVisitor(contentType, contentId, visitorId);
        
        Map<String, Object> response = Map.of(
            "success", true,
            "contentType", contentType,
            "contentId", contentId,
            "newVisitor", isNewVisitor,
            "currentViews", analyticsService.getViewCount(contentType, contentId)
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/views/{contentType}/{contentId}")
    @Operation(
        summary = "Get view count",
        description = "Returns current view count from Redis"
    )
    @ApiResponse(responseCode = "200", description = "View count retrieved")
    public ResponseEntity<Map<String, Object>> getViewCount(
            @PathVariable ContentType contentType,
            @PathVariable Long contentId) {
        
        Long viewCount = analyticsService.getViewCount(contentType, contentId);
        
        return ResponseEntity.ok(Map.of(
            "contentType", contentType,
            "contentId", contentId,
            "viewCount", viewCount
        ));
    }

    @GetMapping("/content/{contentType}/{contentId}")
    @Operation(
        summary = "Get content analytics",
        description = "Returns comprehensive analytics for specific content"
    )
    @ApiResponse(responseCode = "200", description = "Analytics data retrieved")
    public ResponseEntity<Map<String, Object>> getContentAnalytics(
            @PathVariable ContentType contentType,
            @PathVariable Long contentId) {
        
        return ResponseEntity.ok(analyticsService.getContentAnalytics(contentType, contentId));
    }

    @GetMapping("/top/{contentType}")
    @Operation(
        summary = "Get top content by type",
        description = "Returns top content IDs from Redis ZSET ranking"
    )
    @ApiResponse(responseCode = "200", description = "Top content IDs")
    public ResponseEntity<Map<String, Object>> getTopContent(
            @PathVariable ContentType contentType,
            @Parameter(description = "Number of items to return")
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Long> topContentIds = analyticsService.getTopContent(contentType, limit);
        
        return ResponseEntity.ok(Map.of(
            "contentType", contentType,
            "topContentIds", topContentIds,
            "count", topContentIds.size()
        ));
    }

    @GetMapping("/platform")
    @Operation(
        summary = "Get platform analytics",
        description = "Returns platform-wide analytics summary"
    )
    @ApiResponse(responseCode = "200", description = "Platform analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics() {
        return ResponseEntity.ok(analyticsService.getPlatformAnalytics());
    }

    @GetMapping("/unique-visitors/{contentType}/{contentId}")
    @Operation(
        summary = "Get unique visitors count",
        description = "Returns count of unique visitors today using Redis SET"
    )
    @ApiResponse(responseCode = "200", description = "Unique visitors count")
    public ResponseEntity<Map<String, Object>> getUniqueVisitors(
            @PathVariable ContentType contentType,
            @PathVariable Long contentId) {
        
        Long uniqueVisitors = analyticsService.getUniqueVisitorsCount(contentType, contentId);
        
        return ResponseEntity.ok(Map.of(
            "contentType", contentType,
            "contentId", contentId,
            "uniqueVisitorsToday", uniqueVisitors
        ));
    }

    @DeleteMapping("/clear/{contentType}/{contentId}")
    @Operation(
        summary = "Clear analytics for content",
        description = "Clears all analytics data for specific content (admin only)"
    )
    @ApiResponse(responseCode = "200", description = "Analytics cleared")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearAnalytics(
            @PathVariable ContentType contentType,
            @PathVariable Long contentId) {
        
        analyticsService.clearAnalytics(contentType, contentId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Analytics cleared for " + contentType + " " + contentId
        ));
    }

    /**
     * Generate visitor ID from request (IP + User-Agent hash)
     */
    private String getVisitorId(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        if (userAgent == null) {
            userAgent = "unknown";
        }
        
        return String.valueOf((ip + userAgent).hashCode());
    }
}
