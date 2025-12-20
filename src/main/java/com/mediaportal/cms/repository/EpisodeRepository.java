package com.mediaportal.cms.repository;

import com.mediaportal.cms.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    
    List<Episode> findByPodcastId(Long podcastId);
    
    List<Episode> findByPodcastIdOrderByEpisodeNumberAsc(Long podcastId);
}
