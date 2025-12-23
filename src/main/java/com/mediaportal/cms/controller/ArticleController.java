package com.mediaportal.cms.controller;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.article.CreateArticleRequest;
import com.mediaportal.cms.dto.article.UpdateArticleRequest;
import com.mediaportal.cms.service.ArticleService;
import com.mediaportal.cms.service.AnalyticsService;
import com.mediaportal.cms.security.CustomUserDetailsService;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "API for managing articles")
public class ArticleController {

    private final ArticleService articleService;
    private final AnalyticsService analyticsService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    @Operation(summary = "Create a new article")
    public ResponseEntity<ArticleResponse> create(
            @Valid @RequestBody CreateArticleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userDetailsService.getUserByUsername(userDetails.getUsername());
        ArticleResponse response = articleService.create(request, author.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID")
    public ResponseEntity<ArticleResponse> getById(@PathVariable Long id) {
        ArticleResponse response = articleService.getById(id);
        // Track view in Redis
        analyticsService.incrementViewCount(ContentType.ARTICLE, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all articles")
    public ResponseEntity<List<ArticleResponse>> getAll() {
        return ResponseEntity.ok(articleService.getAll());
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published articles")
    public ResponseEntity<List<ArticleResponse>> getPublished() {
        return ResponseEntity.ok(articleService.getPublished());
    }

    @GetMapping("/search")
    @Operation(summary = "Search articles by title")
    public ResponseEntity<List<ArticleResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(articleService.search(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an article")
    public ResponseEntity<ArticleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request) {
        return ResponseEntity.ok(articleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an article")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
