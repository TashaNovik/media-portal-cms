package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.article.CreateArticleRequest;
import com.mediaportal.cms.dto.article.UpdateArticleRequest;
import com.mediaportal.cms.model.Article;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.repository.ArticleRepository;
import com.mediaportal.cms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public ArticleResponse create(CreateArticleRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + authorId));

        Article article = Article.builder()
                .title(request.getTitle())
                .text(request.getText())
                .author(author)
                .isPublished(request.getIsPublished())
                .build();

        if (Boolean.TRUE.equals(request.getIsPublished())) {
            article.setPublishedAt(LocalDateTime.now());
        }

        Article saved = articleRepository.save(article);
        return mapToResponse(saved);
    }

    @Cacheable(value = "articles", key = "#id")
    @Transactional(readOnly = true)
    public ArticleResponse getById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));
        return mapToResponse(article);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getAll() {
        return articleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getPublished() {
        return articleRepository.findByIsPublishedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "articles", key = "#id")
    @Transactional
    public ArticleResponse update(Long id, UpdateArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found with id: " + id));

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getText() != null) {
            article.setText(request.getText());
        }
        if (request.getIsPublished() != null) {
            if (Boolean.TRUE.equals(request.getIsPublished()) && !Boolean.TRUE.equals(article.getIsPublished())) {
                article.setPublishedAt(LocalDateTime.now());
            }
            article.setIsPublished(request.getIsPublished());
        }

        Article saved = articleRepository.save(article);
        return mapToResponse(saved);
    }

    @CacheEvict(value = "articles", key = "#id")
    @Transactional
    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new EntityNotFoundException("Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> search(String query) {
        return articleRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ArticleResponse mapToResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .text(article.getText())
                .authorId(article.getAuthor() != null ? article.getAuthor().getId() : null)
                .authorUsername(article.getAuthor() != null ? article.getAuthor().getUsername() : null)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .isPublished(article.getIsPublished())
                .viewCount(article.getViewCount())
                .build();
    }
}
