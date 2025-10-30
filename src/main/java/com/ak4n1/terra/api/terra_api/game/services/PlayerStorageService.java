package com.ak4n1.terra.api.terra_api.game.services;

import java.util.List;

import com.ak4n1.terra.api.terra_api.game.dto.ItemDTO;

/**
 * Servicio principal para la gestión del inventario y almacenamiento de personajes.
 * 
 * <p>Este servicio proporciona acceso a los items que poseen los personajes,
 * incluyendo información del catálogo en memoria para una respuesta completa.
 * Es el servicio RECOMENDADO para consultar inventarios desde los controladores.
 * 
 * @see PlayerStorageServiceImpl
 * @see ItemDTO
 * @see com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable
 * @author ak4n1
 * @since 1.0
 */
public interface PlayerStorageService {

    /**
     * Obtiene todos los items de un personaje por su ID.
     * 
     * <p>Los items incluyen información completa del catálogo (nombre, tipo, icono, etc.)
     * cargada desde la tabla de items en memoria.
     * 
     * @param playerId ID del personaje (charId)
     * @return Lista de DTOs con todos los items del personaje
     */
    List<ItemDTO> getItemsByPlayerId(int playerId);
    
    /**
     * Método de prueba para verificar el estado del catálogo de items.
     * 
     * @param playerId ID del personaje (no utilizado, parámetro mantenido por compatibilidad)
     * @return Lista con un único DTO informativo sobre el estado del catálogo
     * @deprecated Este método es solo para pruebas. Usar {@link #getItemsByPlayerId(int)} o
     *             {@link com.ak4n1.terra.api.terra_api.game.services.ItemCatalogService#getStats()} en su lugar.
     */
    @Deprecated
    List<ItemDTO> getTest(int playerId);
    
    /**
     * Fuerza la recarga de todos los items desde los archivos XML.
     * 
     * <p>Este método limpia el catálogo actual y vuelve a cargar todos los items
     * desde los archivos XML especificados en la configuración.
     * 
     * @param itemId ID del item (no utilizado, parámetro mantenido por compatibilidad)
     * @return true si la recarga se completó exitosamente
     * @see com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable#reload()
     */
    boolean forceReloadItemFromXml(int itemId);

}
