package com.mediaportal.cms.repository;

import com.mediaportal.cms.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    List<Video> findByIsPublishedTrue();
    
    List<Video> findByAuthorId(Long authorId);
    
    List<Video> findByTitleContainingIgnoreCase(String title);
}
