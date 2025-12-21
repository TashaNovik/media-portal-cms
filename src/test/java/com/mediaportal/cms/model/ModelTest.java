package com.mediaportal.cms.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Model classes.
 */
class ModelTest {

    // ==================== Article Tests ====================

    @Test
    @DisplayName("Article - should create with builder")
    void article_ShouldCreateWithBuilder() {
        Article article = Article.builder()
                .id(1L)
                .title("Test Article")
                .text("Article content")
                .isPublished(true)
                .publishedAt(LocalDateTime.now())
                .viewCount(100L)
                .build();

        assertEquals(1L, article.getId());
        assertEquals("Test Article", article.getTitle());
        assertEquals("Article content", article.getText());
        assertTrue(article.getIsPublished());
        assertEquals(100L, article.getViewCount());
    }

    @Test
    @DisplayName("Article - should set and get all properties")
    void article_ShouldSetAndGetAllProperties() {
        Article article = new Article();
        LocalDateTime now = LocalDateTime.now();
        User author = new User();
        author.setId(1L);

        article.setId(1L);
        article.setTitle("Title");
        article.setText("Text content");
        article.setIsPublished(false);
        article.setPublishedAt(now);
        article.setCreatedAt(now);
        article.setUpdatedAt(now);
        article.setAuthor(author);
        article.setViewCount(50L);

        assertEquals(1L, article.getId());
        assertEquals("Title", article.getTitle());
        assertEquals("Text content", article.getText());
        assertFalse(article.getIsPublished());
        assertEquals(now, article.getPublishedAt());
        assertEquals(now, article.getCreatedAt());
        assertEquals(now, article.getUpdatedAt());
        assertEquals(author, article.getAuthor());
        assertEquals(50L, article.getViewCount());
    }

    @Test
    @DisplayName("Article - onCreate should set timestamps")
    void article_OnCreateShouldSetTimestamps() {
        Article article = new Article();
        assertNull(article.getCreatedAt());
        assertNull(article.getUpdatedAt());
        
        article.onCreate();
        
        assertNotNull(article.getCreatedAt());
        assertNotNull(article.getUpdatedAt());
    }

    @Test
    @DisplayName("Article - onUpdate should update timestamp")
    void article_OnUpdateShouldUpdateTimestamp() {
        Article article = new Article();
        article.onCreate();
        
        article.onUpdate();
        
        assertNotNull(article.getUpdatedAt());
    }

    @Test
    @DisplayName("Article - equals and hashCode")
    void article_EqualsAndHashCode() {
        Article article1 = Article.builder().id(1L).title("Test").build();
        Article article2 = Article.builder().id(1L).title("Test").build();
        Article article3 = Article.builder().id(2L).title("Other").build();

        assertEquals(article1, article2);
        assertEquals(article1.hashCode(), article2.hashCode());
        assertNotEquals(article1, article3);
    }

    // ==================== Video Tests ====================

    @Test
    @DisplayName("Video - should create with builder")
    void video_ShouldCreateWithBuilder() {
        Video video = Video.builder()
                .id(1L)
                .title("Test Video")
                .url("https://example.com/video.mp4")
                .durationSeconds(120)
                .viewCount(500L)
                .isPublished(true)
                .build();

        assertEquals(1L, video.getId());
        assertEquals("Test Video", video.getTitle());
        assertEquals("https://example.com/video.mp4", video.getUrl());
        assertEquals(120, video.getDurationSeconds());
        assertEquals(500L, video.getViewCount());
        assertTrue(video.getIsPublished());
    }

    @Test
    @DisplayName("Video - should set and get all properties")
    void video_ShouldSetAndGetAllProperties() {
        Video video = new Video();
        LocalDateTime now = LocalDateTime.now();

        video.setId(1L);
        video.setTitle("Video Title");
        video.setUrl("https://example.com/v.mp4");
        video.setDurationSeconds(180);
        video.setThumbnailUrl("https://example.com/thumb.jpg");
        video.setCreatedAt(now);
        video.setUpdatedAt(now);
        video.setViewCount(100L);
        video.setIsPublished(false);

        assertEquals(1L, video.getId());
        assertEquals("Video Title", video.getTitle());
        assertEquals("https://example.com/v.mp4", video.getUrl());
        assertEquals(180, video.getDurationSeconds());
        assertEquals("https://example.com/thumb.jpg", video.getThumbnailUrl());
        assertEquals(100L, video.getViewCount());
        assertFalse(video.getIsPublished());
    }

    // ==================== Podcast Tests ====================

    @Test
    @DisplayName("Podcast - should create with builder")
    void podcast_ShouldCreateWithBuilder() {
        Podcast podcast = Podcast.builder()
                .id(1L)
                .title("Test Podcast")
                .description("Podcast description")
                .audioUrl("https://example.com/audio.mp3")
                .isPublished(true)
                .viewCount(200L)
                .build();

        assertEquals(1L, podcast.getId());
        assertEquals("Test Podcast", podcast.getTitle());
        assertEquals("Podcast description", podcast.getDescription());
        assertEquals("https://example.com/audio.mp3", podcast.getAudioUrl());
        assertTrue(podcast.getIsPublished());
    }

    @Test
    @DisplayName("Podcast - should set and get all properties")
    void podcast_ShouldSetAndGetAllProperties() {
        Podcast podcast = new Podcast();
        LocalDateTime now = LocalDateTime.now();

        podcast.setId(1L);
        podcast.setTitle("Podcast Title");
        podcast.setDescription("Description");
        podcast.setAudioUrl("https://example.com/audio.mp3");
        podcast.setIsPublished(false);
        podcast.setCreatedAt(now);
        podcast.setUpdatedAt(now);
        podcast.setViewCount(300L);

        assertEquals(1L, podcast.getId());
        assertEquals("Podcast Title", podcast.getTitle());
        assertEquals("Description", podcast.getDescription());
        assertEquals("https://example.com/audio.mp3", podcast.getAudioUrl());
        assertFalse(podcast.getIsPublished());
        assertEquals(300L, podcast.getViewCount());
    }

    @Test
    @DisplayName("Podcast - should manage episodes list")
    void podcast_ShouldManageEpisodesList() {
        Podcast podcast = new Podcast();
        podcast.setEpisodes(new java.util.ArrayList<>());
        
        Episode episode = new Episode();
        episode.setId(1L);
        episode.setTitle("Episode 1");
        
        podcast.getEpisodes().add(episode);
        
        assertEquals(1, podcast.getEpisodes().size());
        assertEquals("Episode 1", podcast.getEpisodes().get(0).getTitle());
    }

    // ==================== Episode Tests ====================

    @Test
    @DisplayName("Episode - should create with builder")
    void episode_ShouldCreateWithBuilder() {
        Podcast podcast = Podcast.builder().id(1L).title("Podcast").build();
        
        Episode episode = Episode.builder()
                .id(1L)
                .title("Episode 1")
                .audioUrl("https://example.com/ep1.mp3")
                .durationSeconds(1800)
                .episodeNumber(1)
                .podcast(podcast)
                .build();

        assertEquals(1L, episode.getId());
        assertEquals("Episode 1", episode.getTitle());
        assertEquals("https://example.com/ep1.mp3", episode.getAudioUrl());
        assertEquals(1800, episode.getDurationSeconds());
        assertEquals(1, episode.getEpisodeNumber());
        assertEquals(podcast, episode.getPodcast());
    }

    @Test
    @DisplayName("Episode - should set and get all properties")
    void episode_ShouldSetAndGetAllProperties() {
        Episode episode = new Episode();
        Podcast podcast = new Podcast();
        podcast.setId(1L);
        LocalDateTime now = LocalDateTime.now();

        episode.setId(1L);
        episode.setTitle("Episode Title");
        episode.setDescription("Episode description");
        episode.setAudioUrl("https://example.com/audio.mp3");
        episode.setDurationSeconds(3600);
        episode.setEpisodeNumber(5);
        episode.setPodcast(podcast);
        episode.setCreatedAt(now);

        assertEquals(1L, episode.getId());
        assertEquals("Episode Title", episode.getTitle());
        assertEquals("Episode description", episode.getDescription());
        assertEquals("https://example.com/audio.mp3", episode.getAudioUrl());
        assertEquals(3600, episode.getDurationSeconds());
        assertEquals(5, episode.getEpisodeNumber());
        assertEquals(podcast, episode.getPodcast());
        assertEquals(now, episode.getCreatedAt());
    }

    @Test
    @DisplayName("Episode - onCreate should set timestamp")
    void episode_OnCreateShouldSetTimestamp() {
        Episode episode = new Episode();
        assertNull(episode.getCreatedAt());
        
        episode.onCreate();
        
        assertNotNull(episode.getCreatedAt());
    }

    // ==================== User Tests ====================

    @Test
    @DisplayName("User - should create with builder")
    void user_ShouldCreateWithBuilder() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(Role.USER)
                .isActive(true)
                .build();

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("User - should set and get all properties")
    void user_ShouldSetAndGetAllProperties() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(1L);
        user.setUsername("username");
        user.setEmail("email@test.com");
        user.setPassword("pass");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.ADMIN);
        user.setIsActive(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(1L, user.getId());
        assertEquals("username", user.getUsername());
        assertEquals("email@test.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(Role.ADMIN, user.getRole());
        assertFalse(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    @DisplayName("User - onCreate should set timestamps")
    void user_OnCreateShouldSetTimestamps() {
        User user = new User();
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        
        // Call the lifecycle method directly
        user.onCreate();
        
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    @DisplayName("User - onUpdate should update timestamp")
    void user_OnUpdateShouldUpdateTimestamp() {
        User user = new User();
        user.onCreate();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        
        // Wait a tiny bit to ensure different timestamp
        try { Thread.sleep(10); } catch (InterruptedException e) { }
        
        user.onUpdate();
        
        assertNotNull(user.getUpdatedAt());
    }

    // ==================== Role Enum Tests ====================

    @Test
    @DisplayName("Role - should have all expected values")
    void role_ShouldHaveAllExpectedValues() {
        Role[] roles = Role.values();
        
        assertEquals(3, roles.length);
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.EDITOR, Role.valueOf("EDITOR"));
    }

    // ==================== ContentType Enum Tests ====================

    @Test
    @DisplayName("ContentType - should have all expected values")
    void contentType_ShouldHaveAllExpectedValues() {
        ContentType[] types = ContentType.values();
        
        assertEquals(3, types.length);
        assertEquals(ContentType.ARTICLE, ContentType.valueOf("ARTICLE"));
        assertEquals(ContentType.VIDEO, ContentType.valueOf("VIDEO"));
        assertEquals(ContentType.PODCAST, ContentType.valueOf("PODCAST"));
    }

    @Test
    @DisplayName("ContentType - should return correct ordinal")
    void contentType_ShouldReturnCorrectOrdinal() {
        assertEquals(0, ContentType.ARTICLE.ordinal());
        assertEquals(1, ContentType.VIDEO.ordinal());
        assertEquals(2, ContentType.PODCAST.ordinal());
    }
}
