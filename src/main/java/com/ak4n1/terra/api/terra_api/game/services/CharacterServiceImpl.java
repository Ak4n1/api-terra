package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.CharacterResponseDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;
import com.ak4n1.terra.api.terra_api.game.repositories.AccountGameRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de personajes.
 * 
 * <p>Este servicio maneja toda la lógica de negocio relacionada con personajes,
 * incluyendo métodos optimizados para paginación, estadísticas y consultas eficientes.
 * Utiliza JOINs directos entre characters y account_game con paginación a nivel de base de datos.
 * 
 * <p>Características principales:
 * <ul>
 *   <li>Consultas optimizadas con paginación a nivel de BD</li>
 *   <li>Ordenamiento por nivel descendente</li>
 *   <li>Límite de 5 personajes por defecto para vistas rápidas</li>
 *   <li>Cálculo eficiente de estadísticas agregadas</li>
 * </ul>
 * 
 * @see CharacterService
 * @see CharacterRepository
 * @see CharacterResponseDTO
 * @author ak4n1
 * @since 1.0
 */
@Service
public class CharacterServiceImpl implements CharacterService {

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private AccountGameRepository accountGameRepository;

    /**
     * {@inheritDoc}
     * 
     * @param accountName Nombre de la cuenta de juego (login)
     * @return Lista de entidades Character asociadas a la cuenta
     */
    @Override
    public List<Character> getCharactersByAccountName(String accountName) {
        return characterRepository.findByAccountName(accountName);
    }

    /**
     * {@inheritDoc}
     * 
     * @param accountName Nombre de la cuenta de juego (login)
     * @return Lista de DTOs CharacterResponseDTO con la información de los personajes
     */
    @Override
    public List<CharacterResponseDTO> getCharactersDTOByAccountName(String accountName) {
        List<Character> characters = characterRepository.findByAccountName(accountName);
        return characters.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * 
     * <p>Este método está optimizado para mostrar una vista rápida de personajes principales.
     * Utiliza JOIN directo en lugar de múltiples consultas, ordenado por nivel descendente.
     * 
     * @param email Email del usuario
     * @return Lista de máximo 5 personajes ordenados por nivel descendente
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
     * Obtiene personajes con paginación completa.
     * 
     * <p>Este método está diseñado para implementar paginación en el frontend.
     * Utiliza paginación a nivel de base de datos para máxima eficiencia,
     * ordenado por nivel descendente (más alto primero).
     * 
     * @param email Email del usuario
     * @param page Número de página (0-based)
     * @param size Elementos por página
     * @return Página de personajes con metadatos (totalElements, totalPages, etc.)
     */
    public Page<CharacterResponseDTO> getCharactersByEmailWithPagination(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Character> characterPage = characterRepository.findCharactersByEmailWithPagination(email, pageable);
        
        return characterPage.map(this::mapToDTO);
    }

    /**
     * Obtiene estadísticas agregadas de personajes por email.
     * 
     * <p>Calcula estadísticas como total de personajes, número de páginas (asumiendo 5 por página),
     * nivel máximo y nivel promedio. Útil para mostrar dashboards o resúmenes del usuario.
     * 
     * @param email Email del usuario
     * @return Estadísticas calculadas (total, páginas, nivel máximo y promedio)
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
     * Clase interna para estadísticas de personajes.
     * 
     * <p>Usada para transferir estadísticas calculadas al frontend.
     * 
     * @since 1.0
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
     * Mapea una entidad Character a su DTO correspondiente.
     * 
     * <p>Convierte todos los campos de la entidad de base de datos al formato
     * de respuesta para el frontend.
     * 
     * @param character Entidad Character de la base de datos
     * @return DTO CharacterResponseDTO para el frontend
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
