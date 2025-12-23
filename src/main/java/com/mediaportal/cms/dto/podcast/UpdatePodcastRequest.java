package com.mediaportal.cms.dto.podcast;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePodcastRequest {
    
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    @Size(max = 500, message = "Audio URL cannot exceed 500 characters")
    private String audioUrl;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    private Boolean isPublished;
}
