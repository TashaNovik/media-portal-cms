package com.mediaportal.cms.dto.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVideoRequest {
    
    private String title;
    private String url;
    private Integer durationSeconds;
    private String thumbnailUrl;
    private Boolean isPublished;
}
