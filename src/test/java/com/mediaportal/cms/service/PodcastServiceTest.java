package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.podcast.CreatePodcastRequest;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.podcast.UpdatePodcastRequest;
import com.mediaportal.cms.model.Podcast;
import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.repository.PodcastRepository;
import com.mediaportal.cms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PodcastService.
 * Tests CRUD operations for podcasts.
 */
@ExtendWith(MockitoExtension.class)
class PodcastServiceTest {

    @Mock
    private PodcastRepository podcastRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PodcastService podcastService;

    private Podcast testPodcast;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);

        testPodcast = new Podcast();
        testPodcast.setId(1L);
        testPodcast.setTitle("Test Podcast");
        testPodcast.setAudioUrl("https://example.com/podcast.mp3");
        testPodcast.setDescription("Test podcast description");
        testPodcast.setAuthor(testUser);
        testPodcast.setIsPublished(true);
        testPodcast.setViewCount(0L);
        testPodcast.setEpisodes(new ArrayList<>());
        testPodcast.setCreatedAt(LocalDateTime.now());
        testPodcast.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create podcast successfully")
    void create_ShouldSaveAndReturnPodcast() {
        // Given
        CreatePodcastRequest request = new CreatePodcastRequest();
        request.setTitle("New Podcast");
        request.setAudioUrl("https://example.com/new-podcast.mp3");
        request.setDescription("New podcast description");
        request.setIsPublished(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(podcastRepository.save(any(Podcast.class))).thenAnswer(invocation -> {
            Podcast saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setEpisodes(new ArrayList<>());
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // When
        PodcastResponse response = podcastService.create(request, 1L);

        // Then
        assertNotNull(response);
        assertEquals("New Podcast", response.getTitle());
        assertEquals("https://example.com/new-podcast.mp3", response.getAudioUrl());
        assertEquals("New podcast description", response.getDescription());
        assertEquals("testuser", response.getAuthorUsername());

        ArgumentCaptor<Podcast> captor = ArgumentCaptor.forClass(Podcast.class);
        verify(podcastRepository).save(captor.capture());
        assertEquals("New Podcast", captor.getValue().getTitle());
    }

    @Test
    @DisplayName("Should throw exception when creating podcast with non-existent user")
    void create_WhenUserNotFound_ShouldThrowException() {
        // Given
        CreatePodcastRequest request = new CreatePodcastRequest();
        request.setTitle("New Podcast");
        request.setAudioUrl("https://example.com/podcast.mp3");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> podcastService.create(request, 99L));
        verify(podcastRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get podcast by ID")
    void getById_ShouldReturnPodcast() {
        // Given
        when(podcastRepository.findById(1L)).thenReturn(Optional.of(testPodcast));

        // When
        PodcastResponse response = podcastService.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test Podcast", response.getTitle());
        assertEquals("https://example.com/podcast.mp3", response.getAudioUrl());
        assertEquals("Test podcast description", response.getDescription());
        assertEquals("testuser", response.getAuthorUsername());
    }

    @Test
    @DisplayName("Should throw exception when podcast not found")
    void getById_WhenNotFound_ShouldThrowException() {
        // Given
        when(podcastRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> podcastService.getById(99L));
    }

    @Test
    @DisplayName("Should get all podcasts")
    void getAll_ShouldReturnAllPodcasts() {
        // Given
        Podcast podcast2 = new Podcast();
        podcast2.setId(2L);
        podcast2.setTitle("Second Podcast");
        podcast2.setAudioUrl("https://example.com/podcast2.mp3");
        podcast2.setDescription("Second description");
        podcast2.setAuthor(testUser);
        podcast2.setIsPublished(false);
        podcast2.setEpisodes(new ArrayList<>());
        podcast2.setCreatedAt(LocalDateTime.now());
        podcast2.setUpdatedAt(LocalDateTime.now());

        when(podcastRepository.findAll()).thenReturn(Arrays.asList(testPodcast, podcast2));

        // When
        List<PodcastResponse> result = podcastService.getAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Test Podcast", result.get(0).getTitle());
        assertEquals("Second Podcast", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should get published podcasts only")
    void getPublished_ShouldReturnOnlyPublishedPodcasts() {
        // Given
        when(podcastRepository.findByIsPublishedTrue()).thenReturn(List.of(testPodcast));

        // When
        List<PodcastResponse> result = podcastService.getPublished();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsPublished());
        verify(podcastRepository).findByIsPublishedTrue();
    }

    @Test
    @DisplayName("Should update podcast successfully")
    void update_ShouldUpdateAndReturnPodcast() {
        // Given
        UpdatePodcastRequest request = new UpdatePodcastRequest();
        request.setTitle("Updated Title");
        request.setAudioUrl("https://example.com/updated.mp3");
        request.setDescription("Updated description");

        when(podcastRepository.findById(1L)).thenReturn(Optional.of(testPodcast));
        when(podcastRepository.save(any(Podcast.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PodcastResponse response = podcastService.update(1L, request);

        // Then
        assertEquals("Updated Title", response.getTitle());
        assertEquals("https://example.com/updated.mp3", response.getAudioUrl());
        assertEquals("Updated description", response.getDescription());
        verify(podcastRepository).save(any(Podcast.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent podcast")
    void update_WhenNotFound_ShouldThrowException() {
        // Given
        UpdatePodcastRequest request = new UpdatePodcastRequest();
        request.setTitle("New Title");

        when(podcastRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> podcastService.update(99L, request));
    }

    @Test
    @DisplayName("Should delete podcast successfully")
    void delete_ShouldDeletePodcast() {
        // Given
        when(podcastRepository.existsById(1L)).thenReturn(true);

        // When
        podcastService.delete(1L);

        // Then
        verify(podcastRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent podcast")
    void delete_WhenNotFound_ShouldThrowException() {
        // Given
        when(podcastRepository.existsById(99L)).thenReturn(false);

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> podcastService.delete(99L));
        verify(podcastRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should search podcasts by title")
    void search_ShouldReturnMatchingPodcasts() {
        // Given
        when(podcastRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(testPodcast));

        // When
        List<PodcastResponse> result = podcastService.search("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Podcast", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should return empty list when no podcasts match search")
    void search_WhenNoMatch_ShouldReturnEmptyList() {
        // Given
        when(podcastRepository.findByTitleContainingIgnoreCase("NonExistent")).thenReturn(List.of());

        // When
        List<PodcastResponse> result = podcastService.search("NonExistent");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle podcast with null episodes")
    void getById_WhenEpisodesNull_ShouldReturnEmptyList() {
        // Given
        testPodcast.setEpisodes(null);
        when(podcastRepository.findById(1L)).thenReturn(Optional.of(testPodcast));

        // When
        PodcastResponse response = podcastService.getById(1L);

        // Then
        assertNotNull(response);
        assertNotNull(response.getEpisodes());
        assertTrue(response.getEpisodes().isEmpty());
    }
}
