package com.ak4n1.terra.api.terra_api.news.repositories;

import com.ak4n1.terra.api.terra_api.news.entities.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    // Obtener noticias publicadas con ordenamiento personalizado
    @Query("SELECT n FROM News n WHERE n.isPublished = true AND n.language = :language ORDER BY n.isPinned DESC, " +
           "CASE WHEN :sortBy = 'likes' THEN n.likes END DESC, " +
           "CASE WHEN :sortBy = 'oldest' THEN n.publishedAt END ASC, " +
           "CASE WHEN :sortBy = 'newest' THEN n.publishedAt END DESC")
    Page<News> findPublishedNewsByLanguage(@Param("language") String language, 
                                            @Param("sortBy") String sortBy, 
                                            Pageable pageable);

    // Obtener noticia publicada por ID
    Optional<News> findByIdAndIsPublishedTrue(Long id);

    // Incrementar likes
    @Modifying
    @Query("UPDATE News n SET n.likes = n.likes + 1 WHERE n.id = :id")
    void incrementLikes(@Param("id") Long id);

    // Contar noticias publicadas por idioma
    long countByIsPublishedTrueAndLanguage(String language);
}

