package com.mediaportal.cms.repository;

import com.mediaportal.cms.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    List<Article> findByIsPublishedTrue();
    
    List<Article> findByAuthorId(Long authorId);
    
    List<Article> findByTitleContainingIgnoreCase(String title);
}
