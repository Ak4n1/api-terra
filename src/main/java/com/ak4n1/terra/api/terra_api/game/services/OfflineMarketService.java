package com.ak4n1.terra.api.terra_api.game.services;

import java.util.List;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;

/**
 * Servicio principal para la gestión del mercado offline del juego.
 * 
 * <p>Este servicio proporciona acceso a las tiendas offline donde los jugadores
 * pueden vender items mientras están desconectados. Es el servicio RECOMENDADO
 * para consultar información del mercado offline desde los controladores.
 * 
 * @see OfflineMarketServiceImpl
 * @see OfflineStoreDTO
 * @author ak4n1
 * @since 1.0
 */
public interface OfflineMarketService {
    
    /**
     * Obtiene todas las tiendas offline disponibles ordenadas por tiempo descendente.
     * 
     * <p>Cada tienda incluye información del vendedor, items disponibles,
     * precios y metadatos del catálogo de items.
     * 
     * @return Lista de DTOs con todas las tiendas offline disponibles
     */
    List<OfflineStoreDTO> getAllOfflineStores();

    /**
     * Método de prueba para verificar el estado del servicio.
     * 
     * @return Lista vacía de tiendas offline
     * @deprecated Este método es solo para pruebas. Usar {@link #getAllOfflineStores()} en su lugar.
     */
    @Deprecated
    List<OfflineStoreDTO> getTest();

}
