package com.mediaportal.cms.dto.episode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEpisodeRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Audio URL is required")
    private String audioUrl;
    
    private Integer durationSeconds;
    
    @NotNull(message = "Episode number is required")
    private Integer episodeNumber;
    
    @NotNull(message = "Podcast ID is required")
    private Long podcastId;
}
