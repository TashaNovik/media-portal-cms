package com.mediaportal.cms.controller;

import com.mediaportal.cms.dto.podcast.CreatePodcastRequest;
import com.mediaportal.cms.dto.podcast.PodcastResponse;
import com.mediaportal.cms.dto.podcast.UpdatePodcastRequest;
import com.mediaportal.cms.dto.episode.CreateEpisodeRequest;
import com.mediaportal.cms.dto.episode.EpisodeResponse;
import com.mediaportal.cms.service.PodcastService;
import com.mediaportal.cms.service.EpisodeService;
import com.mediaportal.cms.service.AnalyticsService;
import com.mediaportal.cms.security.CustomUserDetailsService;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
@RequiredArgsConstructor
@Tag(name = "Podcasts", description = "API for managing podcasts and episodes")
public class PodcastController {

    private final PodcastService podcastService;
    private final EpisodeService episodeService;
    private final AnalyticsService analyticsService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    @Operation(summary = "Create a new podcast")
    public ResponseEntity<PodcastResponse> create(
            @Valid @RequestBody CreatePodcastRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userDetailsService.getUserByUsername(userDetails.getUsername());
        PodcastResponse response = podcastService.create(request, author.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get podcast by ID")
    public ResponseEntity<PodcastResponse> getById(@PathVariable Long id) {
        PodcastResponse response = podcastService.getById(id);
        analyticsService.incrementViewCount(ContentType.PODCAST, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all podcasts")
    public ResponseEntity<List<PodcastResponse>> getAll() {
        return ResponseEntity.ok(podcastService.getAll());
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published podcasts")
    public ResponseEntity<List<PodcastResponse>> getPublished() {
        return ResponseEntity.ok(podcastService.getPublished());
    }

    @GetMapping("/search")
    @Operation(summary = "Search podcasts by title")
    public ResponseEntity<List<PodcastResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(podcastService.search(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a podcast")
    public ResponseEntity<PodcastResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePodcastRequest request) {
        return ResponseEntity.ok(podcastService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a podcast")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        podcastService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Episode endpoints
    @PostMapping("/{podcastId}/episodes")
    @Operation(summary = "Add episode to podcast")
    public ResponseEntity<EpisodeResponse> addEpisode(
            @PathVariable Long podcastId,
            @Valid @RequestBody CreateEpisodeRequest request) {
        request.setPodcastId(podcastId);
        EpisodeResponse response = episodeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{podcastId}/episodes")
    @Operation(summary = "Get all episodes of a podcast")
    public ResponseEntity<List<EpisodeResponse>> getEpisodes(@PathVariable Long podcastId) {
        return ResponseEntity.ok(episodeService.getByPodcastId(podcastId));
    }

    @DeleteMapping("/episodes/{episodeId}")
    @Operation(summary = "Delete an episode")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long episodeId) {
        episodeService.delete(episodeId);
        return ResponseEntity.noContent().build();
    }
}
