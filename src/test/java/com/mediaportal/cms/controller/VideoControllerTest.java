package com.mediaportal.cms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaportal.cms.dto.video.CreateVideoRequest;
import com.mediaportal.cms.dto.video.UpdateVideoRequest;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.security.CustomUserDetailsService;
import com.mediaportal.cms.service.AnalyticsService;
import com.mediaportal.cms.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for VideoController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VideoService videoService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private VideoResponse testVideo;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        testVideo = VideoResponse.builder()
                .id(1L)
                .title("Test Video")
                .url("https://example.com/video.mp4")
                .durationSeconds(3600)
                .thumbnailUrl("https://example.com/thumb.jpg")
                .authorId(1L)
                .authorUsername("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPublished(true)
                .viewCount(0L)
                .build();
    }

    @Test
    @DisplayName("GET /api/videos - should return all videos")
    void getAll_ShouldReturnAllVideos() throws Exception {
        when(videoService.getAll()).thenReturn(List.of(testVideo));

        mockMvc.perform(get("/api/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Video"))
                .andExpect(jsonPath("$[0].url").value("https://example.com/video.mp4"))
                .andExpect(jsonPath("$[0].durationSeconds").value(3600));
    }

    @Test
    @DisplayName("GET /api/videos/{id} - should return video and track view")
    void getById_ShouldReturnVideoAndTrackView() throws Exception {
        when(videoService.getById(1L)).thenReturn(testVideo);

        mockMvc.perform(get("/api/videos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Video"));

        verify(analyticsService).incrementViewCount(ContentType.VIDEO, 1L);
    }

    @Test
    @DisplayName("GET /api/videos/published - should return published videos")
    void getPublished_ShouldReturnPublishedVideos() throws Exception {
        when(videoService.getPublished()).thenReturn(List.of(testVideo));

        mockMvc.perform(get("/api/videos/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isPublished").value(true));
    }

    @Test
    @DisplayName("GET /api/videos/search - should search videos")
    void search_ShouldReturnMatchingVideos() throws Exception {
        when(videoService.search("Test")).thenReturn(List.of(testVideo));

        mockMvc.perform(get("/api/videos/search")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Video"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/videos - should create video when authenticated")
    void create_WhenAuthenticated_ShouldCreateVideo() throws Exception {
        CreateVideoRequest request = new CreateVideoRequest();
        request.setTitle("New Video");
        request.setUrl("https://example.com/new.mp4");
        request.setDurationSeconds(1800);
        request.setIsPublished(true);

        when(customUserDetailsService.getUserByUsername("testuser")).thenReturn(testUser);
        when(videoService.create(any(CreateVideoRequest.class), eq(1L))).thenReturn(testVideo);

        mockMvc.perform(post("/api/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(videoService).create(any(CreateVideoRequest.class), eq(1L));
    }

    @Test
    @DisplayName("POST /api/videos - should return 401 when not authenticated")
    void create_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        CreateVideoRequest request = new CreateVideoRequest();
        request.setTitle("New Video");
        request.setUrl("https://example.com/video.mp4");

        mockMvc.perform(post("/api/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/videos/{id} - should update video")
    void update_ShouldUpdateVideo() throws Exception {
        UpdateVideoRequest request = new UpdateVideoRequest();
        request.setTitle("Updated Title");

        VideoResponse updated = VideoResponse.builder()
                .id(1L)
                .title("Updated Title")
                .url("https://example.com/video.mp4")
                .isPublished(true)
                .build();

        when(videoService.update(eq(1L), any(UpdateVideoRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/videos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/videos/{id} - should delete video")
    void delete_ShouldDeleteVideo() throws Exception {
        doNothing().when(videoService).delete(1L);

        mockMvc.perform(delete("/api/videos/1"))
                .andExpect(status().isNoContent());

        verify(videoService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/videos/{id} - should return 401 when not authenticated")
    void delete_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/videos/1"))
                .andExpect(status().isUnauthorized());
    }
}
