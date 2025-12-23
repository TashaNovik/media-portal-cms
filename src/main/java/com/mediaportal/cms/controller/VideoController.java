package com.mediaportal.cms.controller;

import com.mediaportal.cms.dto.video.CreateVideoRequest;
import com.mediaportal.cms.dto.video.UpdateVideoRequest;
import com.mediaportal.cms.dto.video.VideoResponse;
import com.mediaportal.cms.service.VideoService;
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
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "API for managing videos")
public class VideoController {

    private final VideoService videoService;
    private final AnalyticsService analyticsService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    @Operation(summary = "Create a new video")
    public ResponseEntity<VideoResponse> create(
            @Valid @RequestBody CreateVideoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userDetailsService.getUserByUsername(userDetails.getUsername());
        VideoResponse response = videoService.create(request, author.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get video by ID")
    public ResponseEntity<VideoResponse> getById(@PathVariable Long id) {
        VideoResponse response = videoService.getById(id);
        analyticsService.incrementViewCount(ContentType.VIDEO, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all videos")
    public ResponseEntity<List<VideoResponse>> getAll() {
        return ResponseEntity.ok(videoService.getAll());
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published videos")
    public ResponseEntity<List<VideoResponse>> getPublished() {
        return ResponseEntity.ok(videoService.getPublished());
    }

    @GetMapping("/search")
    @Operation(summary = "Search videos by title")
    public ResponseEntity<List<VideoResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(videoService.search(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a video")
    public ResponseEntity<VideoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVideoRequest request) {
        return ResponseEntity.ok(videoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a video")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
