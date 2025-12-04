package com.ak4n1.terra.api.terra_api.game.l2j.model.mapregion;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo simplificado de región del mapa para la API.
 * 
 * <p>Representa una región del mapa con su información básica necesaria
 * para determinar la ciudad más cercana basándose en coordenadas.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class MapRegion {
    
    private final String name;
    private final String town;
    private final int locId;
    private final List<int[]> maps; // Lista de coordenadas de mapa [x, y]
    
    /**
     * Constructor principal.
     * 
     * @param name Nombre interno de la región
     * @param town Nombre de la ciudad asociada a la región
     * @param locId ID del mensaje del sistema para mostrar ubicación
     */
    public MapRegion(String name, String town, int locId) {
        this.name = name;
        this.town = town;
        this.locId = locId;
        this.maps = new ArrayList<>();
    }
    
    /**
     * Obtiene el nombre interno de la región.
     * 
     * @return Nombre de la región
     */
    public String getName() {
        return name;
    }
    
    /**
     * Obtiene el nombre de la ciudad asociada a la región.
     * 
     * @return Nombre de la ciudad
     */
    public String getTown() {
        return town;
    }
    
    /**
     * Obtiene el ID del mensaje del sistema.
     * 
     * @return ID del mensaje
     */
    public int getLocId() {
        return locId;
    }
    
    /**
     * Agrega una coordenada de mapa a la región.
     * 
     * @param x Coordenada X del mapa
     * @param y Coordenada Y del mapa
     */
    public void addMap(int x, int y) {
        maps.add(new int[] { x, y });
    }
    
    /**
     * Obtiene todas las coordenadas de mapa de la región.
     * 
     * @return Lista de coordenadas [x, y]
     */
    public List<int[]> getMaps() {
        return maps;
    }
    
    /**
     * Verifica si una coordenada de mapa pertenece a esta región.
     * 
     * @param mapX Coordenada X del mapa
     * @param mapY Coordenada Y del mapa
     * @return true si la coordenada pertenece a la región, false en caso contrario
     */
    public boolean isZoneInRegion(int mapX, int mapY) {
        for (int[] map : maps) {
            if (map[0] == mapX && map[1] == mapY) {
                return true;
            }
        }
        return false;
    }
}

