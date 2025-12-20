package com.mediaportal.cms.dto.video;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVideoRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    private Integer durationSeconds;
    
    private String thumbnailUrl;
    
    private Boolean isPublished = false;
}
