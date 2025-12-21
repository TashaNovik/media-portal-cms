package com.mediaportal.cms.dto;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.article.CreateArticleRequest;
import com.mediaportal.cms.dto.article.UpdateArticleRequest;
import com.mediaportal.cms.dto.auth.LoginRequest;
import com.mediaportal.cms.dto.auth.RegisterRequest;
import com.mediaportal.cms.dto.common.ApiResponse;
import com.mediaportal.cms.dto.video.CreateVideoRequest;
import com.mediaportal.cms.dto.video.UpdateVideoRequest;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.dto.podcast.CreatePodcastRequest;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.episode.CreateEpisodeRequest;
import com.mediaportal.cms.dto.episode.EpisodeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DTO classes.
 */
class DtoTest {

    // ==================== Article DTOs ====================

    @Test
    @DisplayName("CreateArticleRequest - should create with builder")
    void createArticleRequest_ShouldCreateWithBuilder() {
        CreateArticleRequest request = CreateArticleRequest.builder()
                .title("Test Title")
                .text("Test content")
                .isPublished(true)
                .build();

        assertEquals("Test Title", request.getTitle());
        assertEquals("Test content", request.getText());
        assertTrue(request.getIsPublished());
    }

    @Test
    @DisplayName("CreateArticleRequest - should set and get properties")
    void createArticleRequest_ShouldSetAndGetProperties() {
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("Title");
        request.setText("Text");
        request.setIsPublished(false);

        assertEquals("Title", request.getTitle());
        assertEquals("Text", request.getText());
        assertFalse(request.getIsPublished());
    }

    @Test
    @DisplayName("UpdateArticleRequest - should create with builder")
    void updateArticleRequest_ShouldCreateWithBuilder() {
        UpdateArticleRequest request = UpdateArticleRequest.builder()
                .title("Updated Title")
                .text("Updated text")
                .isPublished(true)
                .build();

        assertEquals("Updated Title", request.getTitle());
        assertEquals("Updated text", request.getText());
        assertTrue(request.getIsPublished());
    }

    @Test
    @DisplayName("ArticleResponse - should create with builder")
    void articleResponse_ShouldCreateWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ArticleResponse response = ArticleResponse.builder()
                .id(1L)
                .title("Article")
                .text("Content")
                .authorUsername("author")
                .authorId(1L)
                .createdAt(now)
                .updatedAt(now)
                .publishedAt(now)
                .isPublished(true)
                .viewCount(100L)
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Article", response.getTitle());
        assertEquals("Content", response.getText());
        assertEquals("author", response.getAuthorUsername());
        assertEquals(1L, response.getAuthorId());
        assertEquals(now, response.getCreatedAt());
        assertTrue(response.getIsPublished());
        assertEquals(100L, response.getViewCount());
    }

    // ==================== Video DTOs ====================

    @Test
    @DisplayName("CreateVideoRequest - should create with builder")
    void createVideoRequest_ShouldCreateWithBuilder() {
        CreateVideoRequest request = CreateVideoRequest.builder()
                .title("Video Title")
                .url("https://example.com/video.mp4")
                .durationSeconds(120)
                .thumbnailUrl("https://example.com/thumb.jpg")
                .isPublished(true)
                .build();

        assertEquals("Video Title", request.getTitle());
        assertEquals("https://example.com/video.mp4", request.getUrl());
        assertEquals(120, request.getDurationSeconds());
        assertEquals("https://example.com/thumb.jpg", request.getThumbnailUrl());
        assertTrue(request.getIsPublished());
    }

    @Test
    @DisplayName("UpdateVideoRequest - should create with builder")
    void updateVideoRequest_ShouldCreateWithBuilder() {
        UpdateVideoRequest request = UpdateVideoRequest.builder()
                .title("Updated Video")
                .build();

        assertEquals("Updated Video", request.getTitle());
    }

    @Test
    @DisplayName("VideoResponse - should create with builder")
    void videoResponse_ShouldCreateWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        VideoResponse response = VideoResponse.builder()
                .id(1L)
                .title("Video")
                .url("https://example.com/v.mp4")
                .durationSeconds(60)
                .thumbnailUrl("https://example.com/t.jpg")
                .authorUsername("author")
                .authorId(1L)
                .createdAt(now)
                .viewCount(50L)
                .isPublished(true)
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Video", response.getTitle());
        assertEquals("https://example.com/v.mp4", response.getUrl());
        assertEquals(60, response.getDurationSeconds());
        assertEquals(50L, response.getViewCount());
        assertTrue(response.getIsPublished());
    }

    // ==================== Auth DTOs ====================

    @Test
    @DisplayName("LoginRequest - should create with builder")
    void loginRequest_ShouldCreateWithBuilder() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        assertEquals("testuser", request.getUsername());
        assertEquals("password123", request.getPassword());
    }

    @Test
    @DisplayName("LoginRequest - should set and get properties")
    void loginRequest_ShouldSetAndGetProperties() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("pass");

        assertEquals("user", request.getUsername());
        assertEquals("pass", request.getPassword());
    }

    @Test
    @DisplayName("RegisterRequest - should create with builder")
    void registerRequest_ShouldCreateWithBuilder() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("securepassword")
                .build();

        assertEquals("newuser", request.getUsername());
        assertEquals("new@example.com", request.getEmail());
        assertEquals("securepassword", request.getPassword());
    }

    @Test
    @DisplayName("RegisterRequest - should set and get properties")
    void registerRequest_ShouldSetAndGetProperties() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setEmail("user@test.com");
        request.setPassword("password");

        assertEquals("user", request.getUsername());
        assertEquals("user@test.com", request.getEmail());
        assertEquals("password", request.getPassword());
    }

    // ==================== Podcast DTOs ====================

    @Test
    @DisplayName("CreatePodcastRequest - should create with builder")
    void createPodcastRequest_ShouldCreateWithBuilder() {
        CreatePodcastRequest request = CreatePodcastRequest.builder()
                .title("Podcast Title")
                .description("Podcast description")
                .audioUrl("https://example.com/audio.mp3")
                .isPublished(true)
                .build();

        assertEquals("Podcast Title", request.getTitle());
        assertEquals("Podcast description", request.getDescription());
        assertEquals("https://example.com/audio.mp3", request.getAudioUrl());
        assertTrue(request.getIsPublished());
    }

    @Test
    @DisplayName("PodcastResponse - should create with builder")
    void podcastResponse_ShouldCreateWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        PodcastResponse response = PodcastResponse.builder()
                .id(1L)
                .title("Podcast")
                .description("Desc")
                .audioUrl("https://example.com/audio.mp3")
                .authorUsername("author")
                .authorId(1L)
                .createdAt(now)
                .isPublished(true)
                .viewCount(100L)
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Podcast", response.getTitle());
        assertEquals("Desc", response.getDescription());
        assertEquals("https://example.com/audio.mp3", response.getAudioUrl());
        assertTrue(response.getIsPublished());
        assertEquals(100L, response.getViewCount());
    }

    // ==================== Episode DTOs ====================

    @Test
    @DisplayName("CreateEpisodeRequest - should create with builder")
    void createEpisodeRequest_ShouldCreateWithBuilder() {
        CreateEpisodeRequest request = CreateEpisodeRequest.builder()
                .title("Episode 1")
                .description("First episode")
                .audioUrl("https://example.com/ep1.mp3")
                .durationSeconds(1800)
                .episodeNumber(1)
                .podcastId(1L)
                .build();

        assertEquals("Episode 1", request.getTitle());
        assertEquals("First episode", request.getDescription());
        assertEquals("https://example.com/ep1.mp3", request.getAudioUrl());
        assertEquals(1800, request.getDurationSeconds());
        assertEquals(1, request.getEpisodeNumber());
        assertEquals(1L, request.getPodcastId());
    }

    @Test
    @DisplayName("EpisodeResponse - should create with builder")
    void episodeResponse_ShouldCreateWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        EpisodeResponse response = EpisodeResponse.builder()
                .id(1L)
                .title("Episode")
                .description("Desc")
                .audioUrl("https://example.com/audio.mp3")
                .durationSeconds(3600)
                .episodeNumber(5)
                .podcastId(1L)
                .createdAt(now)
                .build();

        assertEquals(1L, response.getId());
        assertEquals("Episode", response.getTitle());
        assertEquals("https://example.com/audio.mp3", response.getAudioUrl());
        assertEquals(3600, response.getDurationSeconds());
        assertEquals(5, response.getEpisodeNumber());
        assertEquals(1L, response.getPodcastId());
    }

    // ==================== ApiResponse ====================

    @Test
    @DisplayName("ApiResponse - success should create correctly")
    void apiResponse_SuccessShouldCreateCorrectly() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success(data);

        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getError());
    }

    @Test
    @DisplayName("ApiResponse - success with message should create correctly")
    void apiResponse_SuccessWithMessageShouldCreateCorrectly() {
        String data = "test data";
        ApiResponse<String> response = ApiResponse.success(data, "Success message");

        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals("Success message", response.getMessage());
    }

    @Test
    @DisplayName("ApiResponse - error should create correctly")
    void apiResponse_ErrorShouldCreateCorrectly() {
        ApiResponse<Object> response = ApiResponse.error("ERROR_CODE", "Error message");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals("ERROR_CODE", response.getError().getCode());
        assertEquals("Error message", response.getError().getMessage());
    }

    @Test
    @DisplayName("ApiResponse - error with details should create correctly")
    void apiResponse_ErrorWithDetailsShouldCreateCorrectly() {
        ApiResponse<Object> response = ApiResponse.error("ERROR_CODE", "Error message", "Error details");

        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertEquals("ERROR_CODE", response.getError().getCode());
        assertEquals("Error message", response.getError().getMessage());
        assertEquals("Error details", response.getError().getDetails());
    }

    @Test
    @DisplayName("ApiResponse.ErrorDetails - should set and get properties")
    void errorDetails_ShouldSetAndGetProperties() {
        ApiResponse.ErrorDetails details = new ApiResponse.ErrorDetails();
        details.setCode("TEST_CODE");
        details.setMessage("Test message");
        details.setDetails("Test details");

        assertEquals("TEST_CODE", details.getCode());
        assertEquals("Test message", details.getMessage());
        assertEquals("Test details", details.getDetails());
    }

    @Test
    @DisplayName("ApiResponse.ErrorDetails - should create with builder")
    void errorDetails_ShouldCreateWithBuilder() {
        ApiResponse.ErrorDetails details = ApiResponse.ErrorDetails.builder()
                .code("CODE")
                .message("Message")
                .details("Details")
                .build();

        assertEquals("CODE", details.getCode());
        assertEquals("Message", details.getMessage());
        assertEquals("Details", details.getDetails());
    }
}
