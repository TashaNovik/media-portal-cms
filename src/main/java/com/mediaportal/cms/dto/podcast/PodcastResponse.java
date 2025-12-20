package com.mediaportal.cms.dto.podcast;

import com.mediaportal.cms.dto.episode.EpisodeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PodcastResponse {
    
    private Long id;
    private String title;
    private String audioUrl;
    private String description;
    private String authorUsername;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private Long viewCount;
    private List<EpisodeResponse> episodes;
}
