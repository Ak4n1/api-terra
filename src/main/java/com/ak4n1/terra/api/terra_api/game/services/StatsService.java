package com.ak4n1.terra.api.terra_api.game.services;

/**
 * Servicio principal para obtener estadísticas generales del juego.
 * 
 * <p>Este servicio proporciona acceso a estadísticas agregadas como el número
 * de personajes en línea y el total de personajes creados. Es el servicio
 * RECOMENDADO para consultar estadísticas desde los controladores.
 * 
 * @see StatsServiceImpl
 * @author ak4n1
 * @since 1.0
 */
public interface StatsService {
    
    /**
     * Obtiene el número total de personajes que están actualmente en línea.
     * 
     * @return Número de personajes online
     */
    long getOnlineCharacterCount();
    
    /**
     * Obtiene el número total de personajes creados en el servidor.
     * 
     * @return Número total de personajes
     */
    long getTotalCharacterCount();
}
