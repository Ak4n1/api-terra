package com.ak4n1.terra.api.terra_api.news.dto;

import java.util.List;

/**
 * DTO para paginación de noticias
 */
public class NewsPageResponse {
    
    private List<NewsListDTO> news;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    // Getters and Setters

    public List<NewsListDTO> getNews() {
        return news;
    }

    public void setNews(List<NewsListDTO> news) {
        this.news = news;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

