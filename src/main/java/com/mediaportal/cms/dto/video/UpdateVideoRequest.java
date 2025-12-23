package com.mediaportal.cms.dto.video;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVideoRequest {
    
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;
    
    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationSeconds;
    
    @Size(max = 500, message = "Thumbnail URL cannot exceed 500 characters")
    private String thumbnailUrl;
    
    private Boolean isPublished;
}
