package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.episode.EpisodeResponse;
import com.mediaportal.cms.dto.podcast.CreatePodcastRequest;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.podcast.UpdatePodcastRequest;
import com.mediaportal.cms.model.Podcast;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.repository.PodcastRepository;
import com.mediaportal.cms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PodcastService {

    private final PodcastRepository podcastRepository;
    private final UserRepository userRepository;

    @Transactional
    public PodcastResponse create(CreatePodcastRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + authorId));

        Podcast podcast = Podcast.builder()
                .title(request.getTitle())
                .audioUrl(request.getAudioUrl())
                .description(request.getDescription())
                .author(author)
                .isPublished(request.getIsPublished())
                .build();

        Podcast saved = podcastRepository.save(podcast);
        return mapToResponse(saved);
    }

    @Cacheable(value = "podcasts", key = "#id")
    @Transactional(readOnly = true)
    public PodcastResponse getById(Long id) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast not found with id: " + id));
        return mapToResponse(podcast);
    }

    @Transactional(readOnly = true)
    public List<PodcastResponse> getAll() {
        return podcastRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PodcastResponse> getPublished() {
        return podcastRepository.findByIsPublishedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "podcasts", key = "#id")
    @Transactional
    public PodcastResponse update(Long id, UpdatePodcastRequest request) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Podcast not found with id: " + id));

        if (request.getTitle() != null) {
            podcast.setTitle(request.getTitle());
        }
        if (request.getAudioUrl() != null) {
            podcast.setAudioUrl(request.getAudioUrl());
        }
        if (request.getDescription() != null) {
            podcast.setDescription(request.getDescription());
        }
        if (request.getIsPublished() != null) {
            podcast.setIsPublished(request.getIsPublished());
        }

        Podcast saved = podcastRepository.save(podcast);
        return mapToResponse(saved);
    }

    @CacheEvict(value = "podcasts", key = "#id")
    @Transactional
    public void delete(Long id) {
        if (!podcastRepository.existsById(id)) {
            throw new EntityNotFoundException("Podcast not found with id: " + id);
        }
        podcastRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PodcastResponse> search(String query) {
        return podcastRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PodcastResponse mapToResponse(Podcast podcast) {
        List<EpisodeResponse> episodeResponses = podcast.getEpisodes() != null
                ? podcast.getEpisodes().stream()
                .map(episode -> EpisodeResponse.builder()
                        .id(episode.getId())
                        .title(episode.getTitle())
                        .description(episode.getDescription())
                        .audioUrl(episode.getAudioUrl())
                        .durationSeconds(episode.getDurationSeconds())
                        .episodeNumber(episode.getEpisodeNumber())
                        .podcastId(podcast.getId())
                        .createdAt(episode.getCreatedAt())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return PodcastResponse.builder()
                .id(podcast.getId())
                .title(podcast.getTitle())
                .audioUrl(podcast.getAudioUrl())
                .description(podcast.getDescription())
                .authorId(podcast.getAuthor() != null ? podcast.getAuthor().getId() : null)
                .authorUsername(podcast.getAuthor() != null ? podcast.getAuthor().getUsername() : null)
                .createdAt(podcast.getCreatedAt())
                .updatedAt(podcast.getUpdatedAt())
                .isPublished(podcast.getIsPublished())
                .viewCount(podcast.getViewCount())
                .episodes(episodeResponses)
                .build();
    }
}
