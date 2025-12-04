package com.ak4n1.terra.api.terra_api.news.services;

import com.ak4n1.terra.api.terra_api.news.dto.CreateNewsRequest;
import com.ak4n1.terra.api.terra_api.news.dto.NewsDTO;
import com.ak4n1.terra.api.terra_api.news.dto.NewsListDTO;
import com.ak4n1.terra.api.terra_api.news.dto.NewsPageResponse;

import java.util.List;

/**
 * Servicio para gestión de noticias
 * 
 * @author ak4n1
 * @since 1.0
 */
public interface NewsService {

    /**
     * Obtiene las últimas noticias publicadas (para widget)
     * 
     * @param limit Número máximo de noticias a retornar
     * @param language Código de idioma (en, es)
     * @param userEmail Email del usuario autenticado
     * @return Lista de noticias
     */
    List<NewsListDTO> getLatestNews(int limit, String language, String userEmail);

    /**
     * Obtiene noticias publicadas con paginación
     * 
     * @param page Número de página (inicia en 1)
     * @param pageSize Cantidad de noticias por página
     * @param language Código de idioma (en, es)
     * @param sortBy Ordenamiento (newest, oldest, likes)
     * @param userEmail Email del usuario autenticado
     * @return Respuesta paginada con noticias
     */
    NewsPageResponse getNewsList(int page, int pageSize, String language, String sortBy, String userEmail);

    /**
     * Obtiene una noticia publicada por ID
     * 
     * @param id ID de la noticia
     * @return DTO con detalles completos de la noticia
     */
    NewsDTO getNewsById(Long id);

    /**
     * Crea una nueva noticia (Admin)
     * 
     * @param request Datos de la noticia a crear
     * @return ID de la noticia creada
     */
    Long createNews(CreateNewsRequest request);

    /**
     * Actualiza una noticia existente (Admin)
     * 
     * @param id ID de la noticia
     * @param request Datos actualizados
     */
    void updateNews(Long id, CreateNewsRequest request);

    /**
     * Elimina una noticia (Admin)
     * 
     * @param id ID de la noticia
     */
    void deleteNews(Long id);

    /**
     * Publica una noticia (Admin)
     * 
     * @param id ID de la noticia
     */
    void publishNews(Long id);

    /**
     * Despublica una noticia (Admin)
     * 
     * @param id ID de la noticia
     */
    void unpublishNews(Long id);
}

