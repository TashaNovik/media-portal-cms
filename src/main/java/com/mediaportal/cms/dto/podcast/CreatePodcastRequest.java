package com.mediaportal.cms.dto.podcast;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePodcastRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String audioUrl;
    
    private String description;
    
    private Boolean isPublished = false;
}
