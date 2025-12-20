package com.mediaportal.cms.controller;

import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AnalyticsController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("POST /api/analytics/view/{contentType}/{contentId} - should track view")
    void trackView_ShouldReturnSuccess() throws Exception {
        // Given
        when(analyticsService.trackUniqueVisitor(any(ContentType.class), anyLong(), anyString()))
                .thenReturn(true);
        when(analyticsService.getViewCount(ContentType.ARTICLE, 1L)).thenReturn(5L);

        // When/Then
        mockMvc.perform(post("/api/analytics/view/ARTICLE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.contentType").value("ARTICLE"))
                .andExpect(jsonPath("$.contentId").value(1))
                .andExpect(jsonPath("$.newVisitor").value(true))
                .andExpect(jsonPath("$.currentViews").value(5));

        verify(analyticsService).incrementViewCount(ContentType.ARTICLE, 1L);
    }

    @Test
    @DisplayName("GET /api/analytics/views/{contentType}/{contentId} - should return view count")
    void getViewCount_ShouldReturnCount() throws Exception {
        // Given
        when(analyticsService.getViewCount(ContentType.VIDEO, 5L)).thenReturn(100L);

        // When/Then
        mockMvc.perform(get("/api/analytics/views/VIDEO/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("VIDEO"))
                .andExpect(jsonPath("$.contentId").value(5))
                .andExpect(jsonPath("$.viewCount").value(100));
    }

    @Test
    @DisplayName("GET /api/analytics/content/{contentType}/{contentId} - should return analytics")
    void getContentAnalytics_ShouldReturnAnalytics() throws Exception {
        // Given
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("contentType", "ARTICLE");
        analytics.put("contentId", 1L);
        analytics.put("totalViews", 150L);
        analytics.put("uniqueVisitorsToday", 50L);
        analytics.put("currentRank", 3L);

        when(analyticsService.getContentAnalytics(ContentType.ARTICLE, 1L)).thenReturn(analytics);

        // When/Then
        mockMvc.perform(get("/api/analytics/content/ARTICLE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("ARTICLE"))
                .andExpect(jsonPath("$.totalViews").value(150))
                .andExpect(jsonPath("$.uniqueVisitorsToday").value(50))
                .andExpect(jsonPath("$.currentRank").value(3));
    }

    @Test
    @DisplayName("GET /api/analytics/top/{contentType} - should return top content IDs")
    void getTopContent_ShouldReturnTopIds() throws Exception {
        // Given
        when(analyticsService.getTopContent(ContentType.PODCAST, 5))
                .thenReturn(Arrays.asList(10L, 8L, 5L, 3L, 1L));

        // When/Then
        mockMvc.perform(get("/api/analytics/top/PODCAST")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("PODCAST"))
                .andExpect(jsonPath("$.topContentIds").isArray())
                .andExpect(jsonPath("$.topContentIds[0]").value(10))
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @DisplayName("GET /api/analytics/unique-visitors/{contentType}/{contentId} - should return count")
    void getUniqueVisitors_ShouldReturnCount() throws Exception {
        // Given
        when(analyticsService.getUniqueVisitorsCount(ContentType.ARTICLE, 1L)).thenReturn(42L);

        // When/Then
        mockMvc.perform(get("/api/analytics/unique-visitors/ARTICLE/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("ARTICLE"))
                .andExpect(jsonPath("$.contentId").value(1))
                .andExpect(jsonPath("$.uniqueVisitorsToday").value(42));
    }

    @Test
    @DisplayName("Invalid content type should return 400")
    void trackView_WithInvalidContentType_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/analytics/view/INVALID_TYPE/1"))
                .andExpect(status().isBadRequest());
    }
}
