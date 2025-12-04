package com.ak4n1.terra.api.terra_api.game.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para consultar la tabla clan_data directamente sin usar entidad JPA.
 * 
 * <p>Este servicio usa JdbcTemplate para hacer consultas SQL nativas a la tabla
 * clan_data sin que Spring trate de mapearla como entidad JPA. Esto evita
 * conflictos con la estructura de la tabla del core del juego.
 * 
 * <p>Úsalo solo para consultas de lectura. No modifica la tabla.
 */
@Service
public class ClanQueryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Obtiene información básica de un clan por su ID.
     * 
     * <p>Este método es read-only y no modifica datos en la base de datos.
     * 
     * @param clanId ID del clan
     * @return Mapa con la información del clan o Optional vacío si no existe
     */
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getClanById(Integer clanId) {
        if (clanId == null || clanId <= 0) {
            return Optional.empty();
        }

        try {
            // Solo campos importantes: ID, nombre, nivel, reputación, castillo, alianza, líder, crestas
            String sql = "SELECT clan_id, clan_name, clan_level, reputation_score, hasCastle, " +
                        "ally_id, ally_name, leader_id, crest_id, crest_large_id, ally_crest_id " +
                        "FROM clan_data WHERE clan_id = ?";
            
            Map<String, Object> clan = jdbcTemplate.queryForObject(sql, new ClanRowMapper(), clanId);
            return Optional.of(clan);
        } catch (Exception e) {
            // Si no se encuentra el clan, retornar Optional vacío
            return Optional.empty();
        }
    }

    /**
     * Obtiene solo el nombre del clan por su ID (más ligero).
     * 
     * <p>Este método es read-only y no modifica datos en la base de datos.
     * 
     * @param clanId ID del clan
     * @return Nombre del clan o null si no existe
     */
    @Transactional(readOnly = true)
    public String getClanName(Integer clanId) {
        if (clanId == null || clanId <= 0) {
            return null;
        }

        try {
            String sql = "SELECT clan_name FROM clan_data WHERE clan_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, clanId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * RowMapper para mapear solo los campos importantes del clan.
     */
    private static class ClanRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> clan = new HashMap<>();
            // Campos importantes del clan
            clan.put("clanId", rs.getInt("clan_id"));
            clan.put("clanName", rs.getString("clan_name"));
            clan.put("clanLevel", rs.getObject("clan_level") != null ? rs.getInt("clan_level") : null);
            clan.put("reputationScore", rs.getInt("reputation_score"));
            clan.put("hasCastle", rs.getObject("hasCastle") != null ? rs.getInt("hasCastle") : null);
            clan.put("allyId", rs.getObject("ally_id") != null ? rs.getInt("ally_id") : null);
            clan.put("allyName", rs.getString("ally_name"));
            clan.put("leaderId", rs.getObject("leader_id") != null ? rs.getInt("leader_id") : null);
            clan.put("crestId", rs.getObject("crest_id") != null ? rs.getInt("crest_id") : null);
            clan.put("crestLargeId", rs.getObject("crest_large_id") != null ? rs.getInt("crest_large_id") : null);
            clan.put("allyCrestId", rs.getObject("ally_crest_id") != null ? rs.getInt("ally_crest_id") : null);
            return clan;
        }
    }
}

