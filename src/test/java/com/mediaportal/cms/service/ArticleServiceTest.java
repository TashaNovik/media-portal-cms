package com.mediaportal.cms.service;

import com.mediaportal.cms.dto.article.ArticleCreateRequest;
import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.article.ArticleUpdateRequest;
import com.mediaportal.cms.model.Article;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.repository.ArticleRepository;
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
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private ArticleService articleService;

    private Article testArticle;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("author@test.com");
        testUser.setName("Test Author");

        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("Test Article");
        testArticle.setContent("Test content");
        testArticle.setAuthor("Test Author");
        testArticle.setPublished(true);
        testArticle.setViewCount(0L);
        testArticle.setCreatedAt(LocalDateTime.now());
        testArticle.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create article successfully")
    void create_ShouldSaveAndReturnArticle() {
        // Given
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("New Article");
        request.setContent("Article content");
        request.setTags(Arrays.asList("java", "spring"));

        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // When
        ArticleResponse response = articleService.create(request, testUser);

        // Then
        assertNotNull(response);
        assertEquals("New Article", response.getTitle());
        assertEquals("Test Author", response.getAuthor());
        
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        verify(articleRepository).save(captor.capture());
        assertEquals("Article content", captor.getValue().getContent());
    }

    @Test
    @DisplayName("Should get article by ID and increment view count")
    void getById_ShouldReturnArticleAndIncrementViews() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);

        // When
        ArticleResponse response = articleService.getById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test Article", response.getTitle());
        verify(analyticsService).incrementViewCount(ContentType.ARTICLE, 1L);
        verify(articleRepository).save(testArticle);
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
        article2.setAuthor("Author 2");
        article2.setCreatedAt(LocalDateTime.now());
        article2.setUpdatedAt(LocalDateTime.now());

        when(articleRepository.findAll()).thenReturn(Arrays.asList(testArticle, article2));

        // When
        List<ArticleResponse> result = articleService.getAll();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should get published articles only")
    void getPublished_ShouldReturnOnlyPublishedArticles() {
        // Given
        when(articleRepository.findByPublishedTrue()).thenReturn(List.of(testArticle));

        // When
        List<ArticleResponse> result = articleService.getPublished();

        // Then
        assertEquals(1, result.size());
        verify(articleRepository).findByPublishedTrue();
    }

    @Test
    @DisplayName("Should update article successfully")
    void update_ShouldUpdateAndReturnArticle() {
        // Given
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated content");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ArticleResponse response = articleService.update(1L, request);

        // Then
        assertEquals("Updated Title", response.getTitle());
        verify(articleRepository).save(any(Article.class));
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
    @DisplayName("Should search articles by keyword")
    void searchByKeyword_ShouldReturnMatchingArticles() {
        // Given
        when(articleRepository.searchByKeyword("test")).thenReturn(List.of(testArticle));

        // When
        List<ArticleResponse> result = articleService.searchByKeyword("test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
    }

    @Test
    @DisplayName("Should get articles by author")
    void getByAuthor_ShouldReturnAuthorArticles() {
        // Given
        when(articleRepository.findByAuthor("Test Author")).thenReturn(List.of(testArticle));

        // When
        List<ArticleResponse> result = articleService.getByAuthor("Test Author");

        // Then
        assertEquals(1, result.size());
    }
}
