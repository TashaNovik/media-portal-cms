package com.mediaportal.cms.dto.podcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePodcastRequest {
    
    private String title;
    private String audioUrl;
    private String description;
    private Boolean isPublished;
}
