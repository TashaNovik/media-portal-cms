package com.mediaportal.cms.dto.article;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateArticleRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Text is required")
    private String text;
    
    private Boolean isPublished = false;
}
