package com.ak4n1.terra.api.terra_api.news.services;

import com.ak4n1.terra.api.terra_api.news.dto.CreateNewsRequest;
import com.ak4n1.terra.api.terra_api.news.dto.NewsDTO;
import com.ak4n1.terra.api.terra_api.news.dto.NewsListDTO;
import com.ak4n1.terra.api.terra_api.news.dto.NewsPageResponse;
import com.ak4n1.terra.api.terra_api.news.entities.News;
import com.ak4n1.terra.api.terra_api.news.repositories.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de noticias
 * 
 * @author ak4n1
 * @since 1.0
 */
@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Override
    public List<NewsListDTO> getLatestNews(int limit, String language, String userEmail) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<News> newsPage = newsRepository.findPublishedNewsByLanguage(language, "newest", pageable);
        
        return newsPage.getContent().stream()
                .map(news -> mapToListDTO(news))
                .collect(Collectors.toList());
    }

    @Override
    public NewsPageResponse getNewsList(int page, int pageSize, String language, String sortBy, String userEmail) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        
        // Validar sortBy
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "newest";
        }
        
        Page<News> newsPage = newsRepository.findPublishedNewsByLanguage(language, sortBy, pageable);
        
        List<NewsListDTO> newsList = newsPage.getContent().stream()
                .map(news -> mapToListDTO(news))
                .collect(Collectors.toList());
        
        NewsPageResponse response = new NewsPageResponse();
        response.setNews(newsList);
        response.setTotal(newsPage.getTotalElements());
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalPages(newsPage.getTotalPages());
        
        return response;
    }

    @Override
    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findByIdAndIsPublishedTrue(id)
                .orElseThrow(() -> new RuntimeException("News not found"));
        
        return mapToDTO(news);
    }

    @Override
    @Transactional
    public Long createNews(CreateNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle());
        news.setSummary(request.getSummary());
        news.setContent(request.getContent());
        news.setImageUrl(request.getImageUrl());
        news.setLanguage(request.getLanguage());
        news.setAuthorName(request.getAuthorName());
        news.setIsPublished(request.getIsPublished());
        news.setIsPinned(request.getIsPinned());
        
        if (request.getIsPublished()) {
            news.setPublishedAt(new Date());
        }
        
        News savedNews = newsRepository.save(news);
        return savedNews.getId();
    }

    @Override
    @Transactional
    public void updateNews(Long id, CreateNewsRequest request) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));
        
        news.setTitle(request.getTitle());
        news.setSummary(request.getSummary());
        news.setContent(request.getContent());
        news.setImageUrl(request.getImageUrl());
        news.setLanguage(request.getLanguage());
        news.setAuthorName(request.getAuthorName());
        news.setIsPinned(request.getIsPinned());
        
        newsRepository.save(news);
    }

    @Override
    @Transactional
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void publishNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));
        
        news.setIsPublished(true);
        news.setPublishedAt(new Date());
        newsRepository.save(news);
    }

    @Override
    @Transactional
    public void unpublishNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found"));
        
        news.setIsPublished(false);
        newsRepository.save(news);
    }

    // Mappers

    private NewsDTO mapToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setSummary(news.getSummary());
        dto.setContent(news.getContent());
        dto.setImageUrl(news.getImageUrl());
        dto.setLanguage(news.getLanguage());
        dto.setAuthorName(news.getAuthorName());
        dto.setIsPinned(news.getIsPinned());
        dto.setPublishedAt(news.getPublishedAt());
        return dto;
    }

    private NewsListDTO mapToListDTO(News news) {
        NewsListDTO dto = new NewsListDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setSummary(news.getSummary());
        dto.setContent(news.getContent());
        dto.setImageUrl(news.getImageUrl());
        dto.setLanguage(news.getLanguage());
        dto.setAuthorName(news.getAuthorName());
        dto.setPublishedAt(news.getPublishedAt());
        dto.setIsPinned(news.getIsPinned());
        return dto;
    }
}

