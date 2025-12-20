package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.episode.CreateEpisodeRequest;
import com.mediaportal.cms.dto.episode.EpisodeResponse;
import com.mediaportal.cms.model.Episode;
import com.mediaportal.cms.model.Podcast;
import com.mediaportal.cms.repository.EpisodeRepository;
import com.mediaportal.cms.repository.PodcastRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EpisodeService {

    private final EpisodeRepository episodeRepository;
    private final PodcastRepository podcastRepository;

    @CacheEvict(value = "podcasts", key = "#request.podcastId")
    @Transactional
    public EpisodeResponse create(CreateEpisodeRequest request) {
        Podcast podcast = podcastRepository.findById(request.getPodcastId())
                .orElseThrow(() -> new EntityNotFoundException("Podcast not found with id: " + request.getPodcastId()));

        Episode episode = Episode.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .audioUrl(request.getAudioUrl())
                .durationSeconds(request.getDurationSeconds())
                .episodeNumber(request.getEpisodeNumber())
                .podcast(podcast)
                .build();

        Episode saved = episodeRepository.save(episode);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public EpisodeResponse getById(Long id) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Episode not found with id: " + id));
        return mapToResponse(episode);
    }

    @Transactional(readOnly = true)
    public List<EpisodeResponse> getByPodcastId(Long podcastId) {
        return episodeRepository.findByPodcastIdOrderByEpisodeNumberAsc(podcastId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Episode not found with id: " + id));
        episodeRepository.delete(episode);
    }

    private EpisodeResponse mapToResponse(Episode episode) {
        return EpisodeResponse.builder()
                .id(episode.getId())
                .title(episode.getTitle())
                .description(episode.getDescription())
                .audioUrl(episode.getAudioUrl())
                .durationSeconds(episode.getDurationSeconds())
                .episodeNumber(episode.getEpisodeNumber())
                .podcastId(episode.getPodcast().getId())
                .createdAt(episode.getCreatedAt())
                .build();
    }
}
