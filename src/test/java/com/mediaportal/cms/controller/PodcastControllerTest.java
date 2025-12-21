package com.mediaportal.cms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaportal.cms.dto.podcast.CreatePodcastRequest;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.podcast.UpdatePodcastRequest;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.security.CustomUserDetailsService;
import com.mediaportal.cms.service.AnalyticsService;
import com.mediaportal.cms.service.PodcastService;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PodcastController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PodcastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PodcastService podcastService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private PodcastResponse testPodcast;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        testPodcast = PodcastResponse.builder()
                .id(1L)
                .title("Test Podcast")
                .audioUrl("https://example.com/podcast.mp3")
                .description("Test description")
                .authorId(1L)
                .authorUsername("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPublished(true)
                .viewCount(0L)
                .episodes(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("GET /api/podcasts - should return all podcasts")
    void getAll_ShouldReturnAllPodcasts() throws Exception {
        when(podcastService.getAll()).thenReturn(List.of(testPodcast));

        mockMvc.perform(get("/api/podcasts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Podcast"))
                .andExpect(jsonPath("$[0].audioUrl").value("https://example.com/podcast.mp3"));
    }

    @Test
    @DisplayName("GET /api/podcasts/{id} - should return podcast and track view")
    void getById_ShouldReturnPodcastAndTrackView() throws Exception {
        when(podcastService.getById(1L)).thenReturn(testPodcast);

        mockMvc.perform(get("/api/podcasts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Podcast"));

        verify(analyticsService).incrementViewCount(ContentType.PODCAST, 1L);
    }

    @Test
    @DisplayName("GET /api/podcasts/published - should return published podcasts")
    void getPublished_ShouldReturnPublishedPodcasts() throws Exception {
        when(podcastService.getPublished()).thenReturn(List.of(testPodcast));

        mockMvc.perform(get("/api/podcasts/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isPublished").value(true));
    }

    @Test
    @DisplayName("GET /api/podcasts/search - should search podcasts")
    void search_ShouldReturnMatchingPodcasts() throws Exception {
        when(podcastService.search("Test")).thenReturn(List.of(testPodcast));

        mockMvc.perform(get("/api/podcasts/search")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Podcast"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/podcasts - should create podcast when authenticated")
    void create_WhenAuthenticated_ShouldCreatePodcast() throws Exception {
        CreatePodcastRequest request = new CreatePodcastRequest();
        request.setTitle("New Podcast");
        request.setAudioUrl("https://example.com/new.mp3");
        request.setDescription("New description");
        request.setIsPublished(true);

        when(customUserDetailsService.getUserByUsername("testuser")).thenReturn(testUser);
        when(podcastService.create(any(CreatePodcastRequest.class), eq(1L))).thenReturn(testPodcast);

        mockMvc.perform(post("/api/podcasts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(podcastService).create(any(CreatePodcastRequest.class), eq(1L));
    }

    @Test
    @DisplayName("POST /api/podcasts - should return 401 when not authenticated")
    void create_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        CreatePodcastRequest request = new CreatePodcastRequest();
        request.setTitle("New Podcast");
        request.setAudioUrl("https://example.com/podcast.mp3");

        mockMvc.perform(post("/api/podcasts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/podcasts/{id} - should update podcast")
    void update_ShouldUpdatePodcast() throws Exception {
        UpdatePodcastRequest request = new UpdatePodcastRequest();
        request.setTitle("Updated Title");

        PodcastResponse updated = PodcastResponse.builder()
                .id(1L)
                .title("Updated Title")
                .audioUrl("https://example.com/podcast.mp3")
                .isPublished(true)
                .episodes(new ArrayList<>())
                .build();

        when(podcastService.update(eq(1L), any(UpdatePodcastRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/podcasts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/podcasts/{id} - should delete podcast")
    void delete_ShouldDeletePodcast() throws Exception {
        doNothing().when(podcastService).delete(1L);

        mockMvc.perform(delete("/api/podcasts/1"))
                .andExpect(status().isNoContent());

        verify(podcastService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/podcasts/{id} - should return 401 when not authenticated")
    void delete_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/podcasts/1"))
                .andExpect(status().isUnauthorized());
    }
}
