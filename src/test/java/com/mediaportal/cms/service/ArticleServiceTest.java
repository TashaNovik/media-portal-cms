package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.article.CreateArticleRequest;
import com.mediaportal.cms.dto.article.UpdateArticleRequest;
import com.mediaportal.cms.model.Article;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.repository.ArticleRepository;
import com.mediaportal.cms.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArticleService.
 * Tests CRUD operations for articles.
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArticleService articleService;

    private Article testArticle;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user using setters (avoid builder issues with inheritance)
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);

        // Create test article using setters
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("Test Article");
        testArticle.setText("Test content for the article");
        testArticle.setAuthor(testUser);
        testArticle.setIsPublished(true);
        testArticle.setPublishedAt(LocalDateTime.now());
        testArticle.setViewCount(0L);
        testArticle.setCreatedAt(LocalDateTime.now());
        testArticle.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create article successfully")
    void create_ShouldSaveAndReturnArticle() {
        // Given
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("New Article");
        request.setText("Article content");
        request.setIsPublished(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // When
        ArticleResponse response = articleService.create(request, 1L);

        // Then
        assertNotNull(response);
        assertEquals("New Article", response.getTitle());
        assertEquals("testuser", response.getAuthorUsername());
        
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertEquals("Article content", captor.getValue().getText());
    }

    @Test
    @DisplayName("Should throw exception when creating article with non-existent user")
    void create_WhenUserNotFound_ShouldThrowException() {
        // Given
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("New Article");
        request.setText("Content");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> articleService.create(request, 99L));
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get article by ID")
    void getById_ShouldReturnArticle() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));

        // When
        ArticleResponse response = articleService.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test Article", response.getTitle());
        assertEquals("Test content for the article", response.getText());
        assertEquals("testuser", response.getAuthorUsername());
    }

    @Test
    @DisplayName("Should throw exception when article not found")
    void getById_WhenNotFound_ShouldThrowException() {
        // Given
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> articleService.getById(99L));
    }

    @Test
    @DisplayName("Should get all articles")
    void getAll_ShouldReturnAllArticles() {
        // Given
        Article article2 = new Article();
        article2.setId(2L);
        article2.setTitle("Second Article");
        article2.setText("Second content");
        article2.setAuthor(testUser);
        article2.setIsPublished(false);
        article2.setCreatedAt(LocalDateTime.now());
        article2.setUpdatedAt(LocalDateTime.now());

        when(articleRepository.findAll()).thenReturn(Arrays.asList(testArticle, article2));

        // When
        List<ArticleResponse> result = articleService.getAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
        assertEquals("Second Article", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should get published articles only")
    void getPublished_ShouldReturnOnlyPublishedArticles() {
        // Given
        when(articleRepository.findByIsPublishedTrue()).thenReturn(List.of(testArticle));

        // When
        List<ArticleResponse> result = articleService.getPublished();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsPublished());
        verify(articleRepository).findByIsPublishedTrue();
    }

    @Test
    @DisplayName("Should update article successfully")
    void update_ShouldUpdateAndReturnArticle() {
        // Given
        UpdateArticleRequest request = new UpdateArticleRequest();
        request.setTitle("Updated Title");
        request.setText("Updated content");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleResponse response = articleService.update(1L, request);

        // Then
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated content", response.getText());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    @DisplayName("Should update article to published and set publishedAt")
    void update_WhenPublishing_ShouldSetPublishedAt() {
        // Given
        testArticle.setIsPublished(false);
        testArticle.setPublishedAt(null);
        
        UpdateArticleRequest request = new UpdateArticleRequest();
        request.setIsPublished(true);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleResponse response = articleService.update(1L, request);

        // Then
        assertTrue(response.getIsPublished());
        assertNotNull(response.getPublishedAt());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent article")
    void update_WhenNotFound_ShouldThrowException() {
        // Given
        UpdateArticleRequest request = new UpdateArticleRequest();
        request.setTitle("New Title");

        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> articleService.update(99L, request));
    }

    @Test
    @DisplayName("Should delete article successfully")
    void delete_ShouldDeleteArticle() {
        // Given
        when(articleRepository.existsById(1L)).thenReturn(true);

        // When
        articleService.delete(1L);

        // Then
        verify(articleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent article")
    void delete_WhenNotFound_ShouldThrowException() {
        // Given
        when(articleRepository.existsById(99L)).thenReturn(false);

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> articleService.delete(99L));
        verify(articleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should search articles by title")
    void search_ShouldReturnMatchingArticles() {
        // Given
        when(articleRepository.findByTitleContainingIgnoreCase("test")).thenReturn(List.of(testArticle));

        // When
        List<ArticleResponse> result = articleService.search("test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should return empty list when no articles match search")
    void search_WhenNoMatch_ShouldReturnEmptyList() {
        // Given
        when(articleRepository.findByTitleContainingIgnoreCase("nonexistent")).thenReturn(List.of());

        // When
        List<ArticleResponse> result = articleService.search("nonexistent");

        // Then
        assertTrue(result.isEmpty());
    }
}
