package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.SkillDTO;
import com.ak4n1.terra.api.terra_api.game.l2j.data.SkillTable;
import com.ak4n1.terra.api.terra_api.game.l2j.model.skill.SkillTemplate;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para obtener los skills de un personaje.
 * 
 * <p>Este servicio consulta directamente la tabla character_skills usando JDBC
 * sin crear entidades JPA, para evitar sobrescribir la base de datos.
 * Combina los datos de la BD con los templates de skills cargados en memoria.
 * 
 * @see SkillTable
 * @see SkillDTO
 * @author ak4n1
 * @since 1.0
 */
@Service
public class PlayerSkillService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerSkillService.class);
    
    @Autowired
    private SkillTable skillTable;
    
    @Resource
    private DataSource dataSource;
    
    /**
     * Obtiene todos los skills de un personaje por su ID.
     * 
     * <p>Consulta la tabla character_skills y combina los datos con los templates
     * de skills cargados en memoria desde los archivos XML.
     * 
     * <p>Este método es read-only y no modifica datos en la base de datos.
     * 
     * @param charId ID del personaje
     * @return Lista de SkillDTO con los skills del personaje
     */
    @Transactional(readOnly = true)
    public List<SkillDTO> getSkillsByCharacterId(int charId) {
        List<SkillDTO> skills = new ArrayList<>();
        
        String sql = "SELECT skill_id, skill_level, class_index FROM character_skills WHERE charId = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, charId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int skillId = rs.getInt("skill_id");
                    int skillLevel = rs.getInt("skill_level");
                    int classIndex = rs.getInt("class_index");
                    
                    // Obtener template del skill desde la tabla en memoria
                    SkillTemplate template = skillTable.getTemplate(skillId);
                    
                    if (template != null) {
                        SkillDTO dto = new SkillDTO();
                        dto.setSkillId(skillId);
                        dto.setName(template.getName());
                        dto.setIcon(template.getIcon());
                        dto.setOperateType(template.getOperateType());
                        dto.setSkillLevel(skillLevel);
                        dto.setMaxLevel(template.getToLevel());
                        dto.setPassive(template.isPassive());
                        dto.setIsMagic(template.isMagic());
                        dto.setIsDebuff(template.isDebuff());
                        dto.setIsToggle(template.isToggle());
                        dto.setIsTransform(template.isTransform());
                        dto.setClassIndex(classIndex);
                        
                        // Debug para skill 393
                        
                        skills.add(dto);
                    } else {
                        logger.warn("❌ Skill {} no encontrado en catálogo XML", skillId);
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error consultando skills del personaje {}: {}", charId, e.getMessage());
            throw new RuntimeException("Error al obtener skills del personaje", e);
        }
        
        return skills;
    }
}
