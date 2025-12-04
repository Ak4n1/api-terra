package com.ak4n1.terra.api.terra_api.news.controllers;

import com.ak4n1.terra.api.terra_api.news.dto.CreateNewsRequest;
import com.ak4n1.terra.api.terra_api.news.dto.NewsDTO;
import com.ak4n1.terra.api.terra_api.news.dto.NewsListDTO;
import com.ak4n1.terra.api.terra_api.news.dto.NewsPageResponse;
import com.ak4n1.terra.api.terra_api.news.services.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ========================================
 * CONTROLADOR DE NOTICIAS - TERRA API
 * ========================================
 *
 * Este controlador maneja todas las operaciones relacionadas con noticias.
 * 
 * ENDPOINTS PÚBLICOS (requieren autenticación):
 * - GET /api/news/latest?limit=5           // Últimas noticias (widget)
 * - GET /api/news?page=1&pageSize=10       // Lista paginada
 * - GET /api/news/{id}                     // Detalle de noticia
 *
 * ENDPOINTS ADMIN (requieren rol ADMIN):
 * - POST /api/news                         // Crear noticia
 * - PUT /api/news/{id}                     // Actualizar noticia
 * - DELETE /api/news/{id}                  // Eliminar noticia
 * - POST /api/news/{id}/publish            // Publicar noticia
 * - POST /api/news/{id}/unpublish          // Despublicar noticia
 *
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * Obtiene las últimas noticias publicadas (para widget)
     * 
     * GET /api/news/latest?limit=5&language=en
     * 
     * @param limit Número máximo de noticias (default: 5)
     * @param language Código de idioma (default: en)
     * @return Lista de noticias
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestNews(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "en") String language,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            List<NewsListDTO> news = newsService.getLatestNews(limit, language, userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", news);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtiene noticias publicadas con paginación
     * 
     * GET /api/news?page=1&pageSize=10&language=en&sortBy=newest
     * 
     * @param page Número de página (inicia en 1)
     * @param pageSize Cantidad de noticias por página
     * @param language Código de idioma (default: en)
     * @param sortBy Ordenamiento (newest, oldest, likes) (default: newest)
     * @return Respuesta paginada con noticias
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNewsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int pageSize,
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "newest") String sortBy,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            NewsPageResponse newsPage = newsService.getNewsList(page, pageSize, language, sortBy, userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", newsPage);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtiene una noticia publicada por ID
     * 
     * GET /api/news/{id}
     * 
     * @param id ID de la noticia
     * @return DTO con detalles completos de la noticia
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNewsById(@PathVariable Long id) {
        try {
            NewsDTO news = newsService.getNewsById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", news);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "News not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================
    // ENDPOINTS ADMIN (Implementar luego con @PreAuthorize("hasRole('ADMIN')"))
    // ========================================

    /**
     * Crea una nueva noticia (Admin)
     * 
     * POST /api/news
     * 
     * @param request Datos de la noticia
     * @return ID de la noticia creada
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")  // Descomentar cuando tengas el sistema de roles
    public ResponseEntity<Map<String, Object>> createNews(@Valid @RequestBody CreateNewsRequest request) {
        try {
            Long newsId = newsService.createNews(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("id", newsId));
            response.put("message", "News created successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Actualiza una noticia existente (Admin)
     * 
     * PUT /api/news/{id}
     * 
     * @param id ID de la noticia
     * @param request Datos actualizados
     * @return Respuesta de éxito
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody CreateNewsRequest request) {
        
        try {
            newsService.updateNews(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "News updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "News not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Elimina una noticia (Admin)
     * 
     * DELETE /api/news/{id}
     * 
     * @param id ID de la noticia
     * @return Respuesta de éxito
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteNews(@PathVariable Long id) {
        try {
            newsService.deleteNews(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "News deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Publica una noticia (Admin)
     * 
     * POST /api/news/{id}/publish
     * 
     * @param id ID de la noticia
     * @return Respuesta de éxito
     */
    @PostMapping("/{id}/publish")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> publishNews(@PathVariable Long id) {
        try {
            newsService.publishNews(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "News published successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "News not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Despublica una noticia (Admin)
     * 
     * POST /api/news/{id}/unpublish
     * 
     * @param id ID de la noticia
     * @return Respuesta de éxito
     */
    @PostMapping("/{id}/unpublish")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> unpublishNews(@PathVariable Long id) {
        try {
            newsService.unpublishNews(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "News unpublished successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "News not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

