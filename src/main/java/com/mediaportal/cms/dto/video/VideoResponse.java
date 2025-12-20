package com.mediaportal.cms.dto.video;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    
    private Long id;
    private String title;
    private String url;
    private Integer durationSeconds;
    private String thumbnailUrl;
    private String authorUsername;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
    private Long viewCount;
}
