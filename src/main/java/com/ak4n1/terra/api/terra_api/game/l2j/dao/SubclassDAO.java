package com.ak4n1.terra.api.terra_api.game.l2j.dao;

import com.ak4n1.terra.api.terra_api.game.dto.SubclassDTO;
import com.ak4n1.terra.api.terra_api.game.l2j.data.ExperienceTable;
import com.ak4n1.terra.api.terra_api.game.l2j.factory.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para operaciones de subclases de L2J.
 * 
 * <p>Implementa patrón Singleton para gestionar acceso a datos de subclases
 * usando JDBC directo sin exponer detalles de implementación de la base de datos.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class SubclassDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(SubclassDAO.class);
    
    // Singleton Holder Pattern (thread-safe lazy initialization)
    private static class SingletonHolder {
        protected static final SubclassDAO INSTANCE = new SubclassDAO();
    }
    
    public static SubclassDAO getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private SubclassDAO() {
        // Constructor privado para singleton
    }
    
    /**
     * Obtiene todas las subclases de un personaje por su ID.
     * 
     * @param charId ID del personaje
     * @return Lista de SubclassDTO con las subclases del personaje
     */
    public List<SubclassDTO> findByCharId(int charId) {
        List<SubclassDTO> subclasses = new ArrayList<>();
        
        String sql = "SELECT class_id, class_index, exp, sp, level, vitality_points, dual_class " +
                     "FROM character_subclasses WHERE charId = ? ORDER BY class_index ASC";
        
        try (Connection conn = DatabaseFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, charId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SubclassDTO dto = new SubclassDTO();
                    dto.setClassId(rs.getInt("class_id"));
                    dto.setClassIndex(rs.getInt("class_index"));
                    long exp = rs.getLong("exp");
                    int level = rs.getInt("level");
                    dto.setExp(exp);
                    dto.setSp(rs.getLong("sp"));
                    dto.setLevel(level);
                    dto.setVitalityPoints(rs.getInt("vitality_points"));
                    dto.setDualClass(rs.getBoolean("dual_class"));
                    
                    // Calcular porcentaje de XP usando ExperienceTable
                    ExperienceTable expTable = ExperienceTable.getInstance();
                    String expPercent = expTable.calculateExpPercent(exp, level);
                    dto.setExpPercent(expPercent);
                    
                    subclasses.add(dto);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error consultando subclases del personaje {}: {}", charId, e.getMessage());
            throw new RuntimeException("Error al obtener subclases del personaje", e);
        }
        
        return subclasses;
    }
}

