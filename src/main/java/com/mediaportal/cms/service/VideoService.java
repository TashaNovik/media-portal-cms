package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.video.CreateVideoRequest;
import com.mediaportal.cms.dto.video.UpdateVideoRequest;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.model.Video;
import com.mediaportal.cms.repository.UserRepository;
import com.mediaportal.cms.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Transactional
    public VideoResponse create(CreateVideoRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + authorId));

        Video video = Video.builder()
                .title(request.getTitle())
                .url(request.getUrl())
                .durationSeconds(request.getDurationSeconds())
                .thumbnailUrl(request.getThumbnailUrl())
                .author(author)
                .isPublished(request.getIsPublished())
                .build();

        Video saved = videoRepository.save(video);
        return mapToResponse(saved);
    }

    @Cacheable(value = "videos", key = "#id")
    @Transactional(readOnly = true)
    public VideoResponse getById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));
        return mapToResponse(video);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> getAll() {
        return videoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> getPublished() {
        return videoRepository.findByIsPublishedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "videos", key = "#id")
    @Transactional
    public VideoResponse update(Long id, UpdateVideoRequest request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found with id: " + id));

        if (request.getTitle() != null) {
            video.setTitle(request.getTitle());
        }
        if (request.getUrl() != null) {
            video.setUrl(request.getUrl());
        }
        if (request.getDurationSeconds() != null) {
            video.setDurationSeconds(request.getDurationSeconds());
        }
        if (request.getThumbnailUrl() != null) {
            video.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsPublished() != null) {
            video.setIsPublished(request.getIsPublished());
        }

        Video saved = videoRepository.save(video);
        return mapToResponse(saved);
    }

    @CacheEvict(value = "videos", key = "#id")
    @Transactional
    public void delete(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new EntityNotFoundException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> search(String query) {
        return videoRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VideoResponse mapToResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .url(video.getUrl())
                .durationSeconds(video.getDurationSeconds())
                .thumbnailUrl(video.getThumbnailUrl())
                .authorId(video.getAuthor() != null ? video.getAuthor().getId() : null)
                .authorUsername(video.getAuthor() != null ? video.getAuthor().getUsername() : null)
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .isPublished(video.getIsPublished())
                .viewCount(video.getViewCount())
                .build();
    }
}
