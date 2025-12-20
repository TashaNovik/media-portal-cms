package com.mediaportal.cms.repository;

import com.mediaportal.cms.model.Podcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    
    List<Podcast> findByIsPublishedTrue();
    
    List<Podcast> findByAuthorId(Long authorId);
    
    List<Podcast> findByTitleContainingIgnoreCase(String title);
}
