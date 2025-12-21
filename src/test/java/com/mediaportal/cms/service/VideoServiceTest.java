package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.video.CreateVideoRequest;
import com.mediaportal.cms.dto.video.UpdateVideoRequest;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.model.Video;
import com.mediaportal.cms.repository.UserRepository;
import com.mediaportal.cms.repository.VideoRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VideoService.
 * Tests CRUD operations for videos.
 */
@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VideoService videoService;

    private Video testVideo;
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

        testVideo = new Video();
        testVideo.setId(1L);
        testVideo.setTitle("Test Video");
        testVideo.setUrl("https://example.com/video.mp4");
        testVideo.setDurationSeconds(3600);
        testVideo.setThumbnailUrl("https://example.com/thumb.jpg");
        testVideo.setAuthor(testUser);
        testVideo.setIsPublished(true);
        testVideo.setViewCount(0L);
        testVideo.setCreatedAt(LocalDateTime.now());
        testVideo.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create video successfully")
    void create_ShouldSaveAndReturnVideo() {
        // Given
        CreateVideoRequest request = new CreateVideoRequest();
        request.setTitle("New Video");
        request.setUrl("https://example.com/new-video.mp4");
        request.setDurationSeconds(1800);
        request.setThumbnailUrl("https://example.com/new-thumb.jpg");
        request.setIsPublished(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // When
        VideoResponse response = videoService.create(request, 1L);

        // Then
        assertNotNull(response);
        assertEquals("New Video", response.getTitle());
        assertEquals("https://example.com/new-video.mp4", response.getUrl());
        assertEquals(1800, response.getDurationSeconds());
        assertEquals("testuser", response.getAuthorUsername());

        ArgumentCaptor<Video> captor = ArgumentCaptor.forClass(Video.class);
        verify(videoRepository).save(captor.capture());
        assertEquals("New Video", captor.getValue().getTitle());
    }

    @Test
    @DisplayName("Should throw exception when creating video with non-existent user")
    void create_WhenUserNotFound_ShouldThrowException() {
        // Given
        CreateVideoRequest request = new CreateVideoRequest();
        request.setTitle("New Video");
        request.setUrl("https://example.com/video.mp4");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> videoService.create(request, 99L));
        verify(videoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get video by ID")
    void getById_ShouldReturnVideo() {
        // Given
        when(videoRepository.findById(1L)).thenReturn(Optional.of(testVideo));

        // When
        VideoResponse response = videoService.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test Video", response.getTitle());
        assertEquals("https://example.com/video.mp4", response.getUrl());
        assertEquals(3600, response.getDurationSeconds());
        assertEquals("testuser", response.getAuthorUsername());
    }

    @Test
    @DisplayName("Should throw exception when video not found")
    void getById_WhenNotFound_ShouldThrowException() {
        // Given
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> videoService.getById(99L));
    }

    @Test
    @DisplayName("Should get all videos")
    void getAll_ShouldReturnAllVideos() {
        // Given
        Video video2 = new Video();
        video2.setId(2L);
        video2.setTitle("Second Video");
        video2.setUrl("https://example.com/video2.mp4");
        video2.setDurationSeconds(2400);
        video2.setAuthor(testUser);
        video2.setIsPublished(false);
        video2.setCreatedAt(LocalDateTime.now());
        video2.setUpdatedAt(LocalDateTime.now());

        when(videoRepository.findAll()).thenReturn(Arrays.asList(testVideo, video2));

        // When
        List<VideoResponse> result = videoService.getAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Test Video", result.get(0).getTitle());
        assertEquals("Second Video", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should get published videos only")
    void getPublished_ShouldReturnOnlyPublishedVideos() {
        // Given
        when(videoRepository.findByIsPublishedTrue()).thenReturn(List.of(testVideo));

        // When
        List<VideoResponse> result = videoService.getPublished();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsPublished());
        verify(videoRepository).findByIsPublishedTrue();
    }

    @Test
    @DisplayName("Should update video successfully")
    void update_ShouldUpdateAndReturnVideo() {
        // Given
        UpdateVideoRequest request = new UpdateVideoRequest();
        request.setTitle("Updated Title");
        request.setUrl("https://example.com/updated.mp4");
        request.setDurationSeconds(4200);

        when(videoRepository.findById(1L)).thenReturn(Optional.of(testVideo));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        VideoResponse response = videoService.update(1L, request);

        // Then
        assertEquals("Updated Title", response.getTitle());
        assertEquals("https://example.com/updated.mp4", response.getUrl());
        assertEquals(4200, response.getDurationSeconds());
        verify(videoRepository).save(any(Video.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent video")
    void update_WhenNotFound_ShouldThrowException() {
        // Given
        UpdateVideoRequest request = new UpdateVideoRequest();
        request.setTitle("New Title");

        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> videoService.update(99L, request));
    }

    @Test
    @DisplayName("Should delete video successfully")
    void delete_ShouldDeleteVideo() {
        // Given
        when(videoRepository.existsById(1L)).thenReturn(true);

        // When
        videoService.delete(1L);

        // Then
        verify(videoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent video")
    void delete_WhenNotFound_ShouldThrowException() {
        // Given
        when(videoRepository.existsById(99L)).thenReturn(false);

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> videoService.delete(99L));
        verify(videoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should search videos by title")
    void search_ShouldReturnMatchingVideos() {
        // Given
        when(videoRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(testVideo));

        // When
        List<VideoResponse> result = videoService.search("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Video", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should return empty list when no videos match search")
    void search_WhenNoMatch_ShouldReturnEmptyList() {
        // Given
        when(videoRepository.findByTitleContainingIgnoreCase("NonExistent")).thenReturn(List.of());

        // When
        List<VideoResponse> result = videoService.search("NonExistent");

        // Then
        assertTrue(result.isEmpty());
    }
}
