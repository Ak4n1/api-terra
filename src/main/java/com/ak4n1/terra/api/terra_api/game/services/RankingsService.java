package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.RankingClanResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPKResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPvPResponseDTO;

import java.util.List;

/**
 * Servicio principal para la gestión de rankings del juego.
 * 
 * <p>Este servicio proporciona acceso a los rankings de PK, PvP y clanes,
 * siempre retornando los top 10 jugadores/clanes ordenados según el criterio
 * correspondiente. Es el servicio RECOMENDADO para consultar rankings desde los controladores.
 * 
 * @see RankingsServiceImpl
 * @see RankingPKResponseDTO
 * @see RankingPvPResponseDTO
 * @see RankingClanResponseDTO
 * @author ak4n1
 * @since 1.0
 */
public interface RankingsService {

    /**
     * Obtiene el ranking de los top 10 jugadores por muertes PK (Player Kill).
     * 
     * <p>Los jugadores están ordenados por cantidad de muertes PK descendente.
     * Si hay menos de 10 jugadores, el ranking se completa con entradas vacías.
     * 
     * @return DTO con el ranking de top 10 PK
     */
    RankingPKResponseDTO getTopPk();
    
    /**
     * Obtiene el ranking de los top 10 jugadores por muertes PvP (Player vs Player).
     * 
     * <p>Los jugadores están ordenados por cantidad de muertes PvP descendente.
     * Si hay menos de 10 jugadores, el ranking se completa con entradas vacías.
     * 
     * @return DTO con el ranking de top 10 PvP
     */
    RankingPvPResponseDTO getTopPvp();

    /**
     * Obtiene el ranking de los top 10 clanes por puntuación de reputación.
     * 
     * <p>Los clanes están ordenados por reputación descendente e incluyen
     * información sobre si poseen castillo. Si hay menos de 10 clanes,
     * el ranking se completa con entradas vacías.
     * 
     * @return DTO con el ranking de top 10 clanes
     */
    RankingClanResponseDTO getTopClans();

}
