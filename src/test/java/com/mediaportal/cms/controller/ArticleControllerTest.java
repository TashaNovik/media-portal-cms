package com.mediaportal.cms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediaportal.cms.dto.article.ArticleResponse;
import com.mediaportal.cms.dto.article.CreateArticleRequest;
import com.mediaportal.cms.dto.article.UpdateArticleRequest;
import com.mediaportal.cms.model.ContentType;
import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.security.CustomUserDetailsService;
import com.mediaportal.cms.service.AnalyticsService;
import com.mediaportal.cms.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ArticleController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private ArticleResponse testArticle;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        testArticle = ArticleResponse.builder()
                .id(1L)
                .title("Test Article")
                .text("Test content")
                .authorId(1L)
                .authorUsername("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isPublished(true)
                .viewCount(0L)
                .build();
    }

    @Test
    @DisplayName("GET /api/articles - should return all articles")
    void getAll_ShouldReturnAllArticles() throws Exception {
        when(articleService.getAll()).thenReturn(List.of(testArticle));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Article"))
                .andExpect(jsonPath("$[0].text").value("Test content"));
    }

    @Test
    @DisplayName("GET /api/articles/{id} - should return article and track view")
    void getById_ShouldReturnArticleAndTrackView() throws Exception {
        when(articleService.getById(1L)).thenReturn(testArticle);

        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Article"));

        verify(analyticsService).incrementViewCount(ContentType.ARTICLE, 1L);
    }

    @Test
    @DisplayName("GET /api/articles/published - should return published articles")
    void getPublished_ShouldReturnPublishedArticles() throws Exception {
        when(articleService.getPublished()).thenReturn(List.of(testArticle));

        mockMvc.perform(get("/api/articles/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isPublished").value(true));
    }

    @Test
    @DisplayName("GET /api/articles/search - should search articles")
    void search_ShouldReturnMatchingArticles() throws Exception {
        when(articleService.search("Test")).thenReturn(List.of(testArticle));

        mockMvc.perform(get("/api/articles/search")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Article"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("POST /api/articles - should create article when authenticated")
    void create_WhenAuthenticated_ShouldCreateArticle() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("New Article");
        request.setText("New content");
        request.setIsPublished(true);

        when(customUserDetailsService.getUserByUsername("testuser")).thenReturn(testUser);
        when(articleService.create(any(CreateArticleRequest.class), eq(1L))).thenReturn(testArticle);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(articleService).create(any(CreateArticleRequest.class), eq(1L));
    }

    @Test
    @DisplayName("POST /api/articles - should return 401 when not authenticated")
    void create_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest();
        request.setTitle("New Article");
        request.setText("New content");

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("PUT /api/articles/{id} - should update article")
    void update_ShouldUpdateArticle() throws Exception {
        UpdateArticleRequest request = new UpdateArticleRequest();
        request.setTitle("Updated Title");

        ArticleResponse updated = ArticleResponse.builder()
                .id(1L)
                .title("Updated Title")
                .text("Test content")
                .isPublished(true)
                .build();

        when(articleService.update(eq(1L), any(UpdateArticleRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/articles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("DELETE /api/articles/{id} - should delete article")
    void delete_ShouldDeleteArticle() throws Exception {
        doNothing().when(articleService).delete(1L);

        mockMvc.perform(delete("/api/articles/1"))
                .andExpect(status().isNoContent());

        verify(articleService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/articles/{id} - should return 401 when not authenticated")
    void delete_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/articles/1"))
                .andExpect(status().isUnauthorized());
    }
}
