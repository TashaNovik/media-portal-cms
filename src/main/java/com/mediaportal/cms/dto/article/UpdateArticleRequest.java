package com.mediaportal.cms.dto.article;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateArticleRequest {
    
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    @Size(min = 1, message = "Text cannot be empty")
    private String text;
    
    private Boolean isPublished;
}
