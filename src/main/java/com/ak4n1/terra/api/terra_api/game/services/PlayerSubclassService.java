package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.SubclassDTO;
import com.ak4n1.terra.api.terra_api.game.l2j.dao.SubclassDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para obtener las subclases de un personaje.
 * 
 * <p>Este servicio usa el DAO de L2J para consultar las subclases
 * sin crear entidades JPA, para evitar sobrescribir la base de datos.
 * 
 * @see SubclassDTO
 * @see SubclassDAO
 * @author ak4n1
 * @since 1.0
 */
@Service
public class PlayerSubclassService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerSubclassService.class);
    
    private final SubclassDAO subclassDAO;
    
    public PlayerSubclassService() {
        this.subclassDAO = SubclassDAO.getInstance();
    }
    
    /**
     * Obtiene todas las subclases de un personaje por su ID.
     * 
     * <p>Consulta la tabla character_subclasses y retorna la lista de subclases
     * incluyendo la clase base (class_index=0) si existe.
     * 
     * <p>Este método es read-only y no modifica datos en la base de datos.
     * 
     * @param charId ID del personaje
     * @return Lista de SubclassDTO con las subclases del personaje
     */
    @Transactional(readOnly = true)
    public List<SubclassDTO> getSubclassesByCharacterId(int charId) {
        return subclassDAO.findByCharId(charId);
    }
}

