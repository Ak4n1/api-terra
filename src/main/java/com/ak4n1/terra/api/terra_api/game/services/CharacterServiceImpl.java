package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.CharacterResponseDTO;
import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import com.ak4n1.terra.api.terra_api.game.entities.Character;
import com.ak4n1.terra.api.terra_api.game.repositories.AccountGameRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ========================================
 * SERVICIO DE PERSONAJES - TERRA API
 * ========================================
 * 
 * Este servicio maneja toda la lógica de negocio relacionada con personajes.
 * Incluye métodos optimizados para paginación, estadísticas y consultas eficientes.
 * 
 * ========================================
 * OPTIMIZACIONES IMPLEMENTADAS
 * ========================================
 * 
 * 1. CONSULTAS OPTIMIZADAS:
 *    - JOIN directo entre characters y account_game
 *    - Paginación a nivel de base de datos
 *    - Ordenamiento por nivel descendente
 * 
 * 2. CACHE Y PERFORMANCE:
 *    - Consultas paginadas para evitar cargar todos los datos
 *    - Estadísticas calculadas eficientemente
 *    - Límite de 5 personajes por defecto
 * 
 * ========================================
 * ESTRUCTURA DE RESPUESTAS PARA ANGULAR
 * ========================================
 * 
 * PAGINACIÓN:
 * {
 *   "content": [CharacterResponseDTO[]],  // Personajes de la página actual
 *   "totalElements": 15,                  // Total en BD
 *   "totalPages": 3,                     // Páginas totales
 *   "currentPage": 0,                    // Página actual (0-based)
 *   "size": 5,                          // Elementos por página
 *   "hasNext": true,                    // Tiene página siguiente
 *   "hasPrevious": false                // Tiene página anterior
 * }
 * 
 * ESTADÍSTICAS:
 * {
 *   "totalCharacters": 15,              // Total de personajes
 *   "totalPages": 3,                    // Páginas (con 5 por página)
 *   "maxLevel": 85,                     // Nivel más alto
 *   "averageLevel": 67.33               // Nivel promedio
 * }
 * 
 * ========================================
 * IMPLEMENTACIÓN EN ANGULAR
 * ========================================
 * 
 * // CharacterService en Angular:
 * interface PaginatedResponse<T> {
 *   content: T[];
 *   totalElements: number;
 *   totalPages: number;
 *   currentPage: number;
 *   size: number;
 *   hasNext: boolean;
 *   hasPrevious: boolean;
 * }
 * 
 * // Método en el servicio Angular:
 * getCharactersPaginated(email: string, page: number, size: number = 5) {
 *   return this.http.get<PaginatedResponse<Character>>(
 *     `${this.apiUrl}/api/game/characters/by-email/paginated?email=${email}&page=${page}&size=${size}`
 *   );
 * }
 * 
 * // En el componente:
 * loadCharacters(page: number = 0) {
 *   this.loading = true;
 *   this.characterService.getCharactersPaginated(this.userEmail, page, 5)
 *     .subscribe({
 *       next: (response) => {
 *         this.characters = response.content;
 *         this.currentPage = response.currentPage;
 *         this.totalPages = response.totalPages;
 *         this.hasNext = response.hasNext;
 *         this.hasPrevious = response.hasPrevious;
 *         this.loading = false;
 *       },
 *       error: (error) => {
 *         console.error('Error:', error);
 *         this.loading = false;
 *       }
 *     });
 * }
 * 
 * // Controles de navegación:
 * nextPage() {
 *   if (this.hasNext) {
 *     this.loadCharacters(this.currentPage + 1);
 *   }
 * }
 * 
 * previousPage() {
 *   if (this.hasPrevious) {
 *     this.loadCharacters(this.currentPage - 1);
 *   }
 * }
 */
@Service
public class CharacterServiceImpl implements CharacterService {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private AccountGameRepository accountGameRepository;

    @Override
    public List<Character> getCharactersByAccountName(String accountName) {
        return characterRepository.findByAccountName(accountName);
    }

    @Override
    public List<CharacterResponseDTO> getCharactersDTOByAccountName(String accountName) {
        List<Character> characters = characterRepository.findByAccountName(accountName);
        return characters.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener personajes por email con límite de 5 (optimizado)
     * 
     * USO: Para mostrar vista rápida de personajes principales
     * OPTIMIZACIÓN: Usa JOIN directo en lugar de múltiples consultas
     * ORDEN: Por nivel descendente (más alto primero)
     * 
     * @param email - Email del usuario
     * @return Lista de máximo 5 personajes
     */
    @Override
    public List<CharacterResponseDTO> getCharactersByEmail(String email) {
        // Usar el método optimizado del repository con límite de 5
        Pageable pageable = PageRequest.of(0, 5); // Página 0, 5 elementos
        List<Character> characters = characterRepository.findCharactersByEmailWithLimit(email, pageable);
        
        return characters.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener personajes con paginación completa
     * 
     * USO: Para implementar paginación en el frontend
     * OPTIMIZACIÓN: Paginación a nivel de base de datos
     * ORDEN: Por nivel descendente (más alto primero)
     * 
     * @param email - Email del usuario
     * @param page - Número de página (0-based)
     * @param size - Elementos por página
     * @return Página de personajes con metadatos
     */
    public Page<CharacterResponseDTO> getCharactersByEmailWithPagination(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Character> characterPage = characterRepository.findCharactersByEmailWithPagination(email, pageable);
        
        return characterPage.map(this::mapToDTO);
    }

    /**
     * Obtener estadísticas de personajes por email
     * 
     * USO: Para mostrar dashboard o resumen del usuario
     * CÁLCULOS: Total, páginas, nivel máximo y promedio
     * 
     * @param email - Email del usuario
     * @return Estadísticas calculadas
     */
    public CharacterStats getCharacterStatsByEmail(String email) {
        long totalCharacters = characterRepository.countCharactersByEmail(email);
        List<Character> allCharacters = characterRepository.findCharactersByEmail(email);
        
        CharacterStats stats = new CharacterStats();
        stats.setTotalCharacters(totalCharacters);
        stats.setTotalPages((int) Math.ceil((double) totalCharacters / 5)); // 5 por página
        
        // Calcular estadísticas adicionales
        if (!allCharacters.isEmpty()) {
            int maxLevel = allCharacters.stream()
                    .mapToInt(Character::getLevel)
                    .max()
                    .orElse(0);
            
            int totalLevel = allCharacters.stream()
                    .mapToInt(Character::getLevel)
                    .sum();
            
            double averageLevel = (double) totalLevel / allCharacters.size();
            
            stats.setMaxLevel(maxLevel);
            stats.setAverageLevel(Math.round(averageLevel * 100.0) / 100.0); // Redondear a 2 decimales
        }
        
        return stats;
    }

    /**
     * Clase para estadísticas de personajes
     * 
     * USO: Para transferir estadísticas calculadas al frontend
     */
    public static class CharacterStats {
        private long totalCharacters;
        private int totalPages;
        private int maxLevel;
        private double averageLevel;

        // Getters y setters
        public long getTotalCharacters() { return totalCharacters; }
        public void setTotalCharacters(long totalCharacters) { this.totalCharacters = totalCharacters; }
        
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        
        public int getMaxLevel() { return maxLevel; }
        public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }
        
        public double getAverageLevel() { return averageLevel; }
        public void setAverageLevel(double averageLevel) { this.averageLevel = averageLevel; }
    }

    /**
     * Mapear entidad Character a DTO
     * 
     * USO: Para convertir datos de la base de datos a formato de respuesta
     * 
     * @param character - Entidad de la base de datos
     * @return DTO para el frontend
     */
    private CharacterResponseDTO mapToDTO(Character character) {
        CharacterResponseDTO dto = new CharacterResponseDTO();

        dto.setCharId(character.getCharId());
        dto.setAccountName(character.getAccountName());
        dto.setCharName(character.getCharName());
        dto.setLevel(character.getLevel());
        dto.setMaxHp(character.getMaxHp());
        dto.setCurHp(character.getCurHp());
        dto.setMaxCp(character.getMaxCp());
        dto.setCurCp(character.getCurCp());
        dto.setMaxMp(character.getMaxMp());
        dto.setCurMp(character.getCurMp());
        dto.setFace(character.getFace());
        dto.setHairStyle(character.getHairStyle());
        dto.setHairColor(character.getHairColor());
        dto.setSex(character.getSex());
        dto.setHeading(character.getHeading());
        dto.setX(character.getX());
        dto.setY(character.getY());
        dto.setZ(character.getZ());
        dto.setExp(character.getExp());
        dto.setExpBeforeDeath(character.getExpBeforeDeath());
        dto.setSp(character.getSp());
        dto.setReputation(character.getReputation());
        dto.setFame(character.getFame());
        dto.setRaidbossPoints(character.getRaidbossPoints());
        dto.setPvpkills(character.getPvpkills());
        dto.setPkkills(character.getPkkills());
        dto.setClanid(character.getClanid());
        dto.setRace(character.getRace());
        dto.setClassid(character.getClassid());
        dto.setBaseClass(character.getBaseClass());
        dto.setTransformId(character.getTransformId());
        dto.setDeleteTime(character.getDeleteTime());
        dto.setCanCraft(character.getCanCraft());
        dto.setTitle(character.getTitle());
        dto.setTitleColor(character.getTitleColor());
        dto.setOnline(character.getOnline());
        dto.setOnlineTime(character.getOnlineTime());
        dto.setCharSlot(character.getCharSlot());
        dto.setLastAccess(character.getLastAccess());
        dto.setClanPrivs(character.getClanPrivs());
        dto.setWantsPeace(character.getWantsPeace());
        dto.setPowerGrade(character.getPowerGrade());
        dto.setNobless(character.getNobless());
        dto.setSubPledge(character.getSubPledge());
        dto.setLvlJoinedAcademy(character.getLvlJoinedAcademy());
        dto.setApprentice(character.getApprentice());
        dto.setSponsor(character.getSponsor());
        dto.setClanJoinExpiryTime(character.getClanJoinExpiryTime());
        dto.setClanCreateExpiryTime(character.getClanCreateExpiryTime());
        dto.setBookmarkSlot(character.getBookmarkSlot());
        dto.setVitalityPoints(character.getVitalityPoints());
        dto.setCreateDate(character.getCreateDate());
        dto.setLanguage(character.getLanguage());
        dto.setFaction(character.getFaction());
        dto.setPcCafePoints(character.getPcCafePoints());

        return dto;
    }
}
