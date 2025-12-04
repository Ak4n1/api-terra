package com.ak4n1.terra.api.terra_api.game.l2j.data;

import com.ak4n1.terra.api.terra_api.game.l2j.model.mapregion.MapRegion;
import com.ak4n1.terra.api.terra_api.game.l2j.util.MapRegionXmlParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabla de regiones del mapa simplificada del core L2J.
 * 
 * <p>Componente principal que carga todas las regiones desde archivos XML en memoria
 * al iniciar la aplicación. Proporciona métodos para obtener la ciudad más cercana
 * basándose en coordenadas X, Y del personaje. Es la clase RECOMENDADA para acceder
 * a las regiones del mapa desde cualquier parte de la aplicación.
 * 
 * <p>Características:
 * <ul>
 *   <li>Carga automática al iniciar Spring Boot mediante @PostConstruct</li>
 *   <li>Búsqueda de región basándose en coordenadas del mapa</li>
 *   <li>Obtención del nombre de la ciudad más cercana</li>
 *   <li>Recarga manual disponible</li>
 * </ul>
 * 
 * <p>La lógica es adaptada del MapRegionManager del core L2J Mobius Classic 3.0.
 * 
 * @see MapRegion
 * @see MapRegionXmlParser
 * @author ak4n1
 * @since 1.0
 */
@Component
public class MapRegionTable {
    
    private static final Logger logger = LoggerFactory.getLogger(MapRegionTable.class);
    
    @Value("${l2j.mapregion.path:classpath:static/mapregion}")
    private String mapregionPath;
    
    private final ResourceLoader resourceLoader;
    private final Map<String, MapRegion> regions = new HashMap<>();
    
    public MapRegionTable(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Inicializa y carga todas las regiones desde los archivos XML.
     * 
     * <p>Este método se ejecuta automáticamente al iniciar Spring Boot mediante
     * @PostConstruct. Lee todos los archivos XML de la ruta configurada y carga
     * las regiones en memoria.
     */
    @PostConstruct
    public void init() {
        logger.info("🔄 Iniciando carga de regiones del mapa desde XMLs...");
        logger.info("📂 Ruta configurada: {}", mapregionPath);
        loadRegions();
    }
    
    /**
     * Carga todas las regiones desde los archivos XML de la ruta configurada.
     * 
     * <p>Procesa todos los archivos XML encontrados en el directorio, parsea cada
     * región y las almacena en un mapa indexado por nombre.
     */
    private void loadRegions() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
            String pattern = mapregionPath.startsWith("classpath:") 
                ? mapregionPath.replace("classpath:", "classpath:") + "/*.xml"
                : mapregionPath + "/*.xml";
            
            Resource[] resources = resolver.getResources(pattern);
            
            if (resources.length == 0) {
                logger.warn("⚠️ No se encontraron archivos XML en: {}", mapregionPath);
                return;
            }
            
            logger.info("📁 Encontrados {} archivos XML para procesar", resources.length);
            
            int totalRegions = 0;
            
            // Parsear todos los archivos
            for (Resource resource : resources) {
                try {
                    java.util.List<MapRegion> parsedRegions;
                    
                    String fileName = resource.getFilename() != null ? resource.getFilename() : "unknown.xml";
                    
                    // Intentar usar File si está disponible (desarrollo)
                    try {
                        File xmlFile = resource.getFile();
                        parsedRegions = MapRegionXmlParser.parseFile(xmlFile);
                    } catch (IOException e) {
                        // Si está dentro del JAR, usar InputStream (producción)
                        try (InputStream inputStream = resource.getInputStream()) {
                            parsedRegions = MapRegionXmlParser.parseFile(inputStream, fileName);
                        }
                    }
                    
                    for (MapRegion region : parsedRegions) {
                        regions.put(region.getName(), region);
                        totalRegions++;
                    }
                    
                    
                } catch (Exception e) {
                    String fileName = resource.getFilename() != null ? resource.getFilename() : "unknown.xml";
                    logger.error("❌ Error procesando archivo {}: {}", fileName, e.getMessage());
                }
            }
            
            logger.info("🎉 Carga de regiones completada:");
            logger.info("   📊 Total regiones cargadas: {}", totalRegions);
            logger.info("   💾 Regiones en memoria: {}", regions.size());
            
        } catch (Exception e) {
            logger.error("❌ Error cargando regiones desde: {}", mapregionPath, e);
        }
    }
    
    /**
     * Obtiene una región del mapa basándose en coordenadas X, Y.
     * 
     * <p>Convierte las coordenadas del mundo en coordenadas de cuadrante de mapa
     * y busca la región que contiene ese cuadrante.
     * 
     * @param locX Coordenada X del mundo
     * @param locY Coordenada Y del mundo
     * @return MapRegion si se encuentra, null en caso contrario
     */
    public MapRegion getMapRegion(int locX, int locY) {
        int mapRegionX = getMapRegionX(locX);
        int mapRegionY = getMapRegionY(locY);
        
        for (MapRegion region : regions.values()) {
            if (region.isZoneInRegion(mapRegionX, mapRegionY)) {
                return region;
            }
        }
        return null;
    }
    
    /**
     * Obtiene el ID del mensaje del sistema para una ubicación.
     * 
     * @param locX Coordenada X del mundo
     * @param locY Coordenada Y del mundo
     * @return ID del mensaje del sistema, o 0 si no se encuentra región
     */
    public int getMapRegionLocId(int locX, int locY) {
        MapRegion region = getMapRegion(locX, locY);
        if (region != null) {
            return region.getLocId();
        }
        return 0;
    }
    
    /**
     * Convierte una coordenada X del mundo en coordenada X del cuadrante de mapa.
     * 
     * <p>Lógica adaptada del core L2J: (posX >> 15) + 9 + 11
     * 
     * @param posX Coordenada X del mundo
     * @return Coordenada X del cuadrante de mapa
     */
    public int getMapRegionX(int posX) {
        return (posX >> 15) + 9 + 11; // + centerTileX
    }
    
    /**
     * Convierte una coordenada Y del mundo en coordenada Y del cuadrante de mapa.
     * 
     * <p>Lógica adaptada del core L2J: (posY >> 15) + 10 + 8
     * 
     * @param posY Coordenada Y del mundo
     * @return Coordenada Y del cuadrante de mapa
     */
    public int getMapRegionY(int posY) {
        return (posY >> 15) + 10 + 8; // + centerTileY
    }
    
    /**
     * Obtiene el nombre de la ciudad más cercana basándose en coordenadas.
     * 
     * <p>Lógica adaptada del método getClosestTownName del MapRegionManager del core.
     * 
     * @param locX Coordenada X del mundo
     * @param locY Coordenada Y del mundo
     * @return Nombre de la ciudad, o "Aden Castle Town" por defecto si no se encuentra
     */
    public String getClosestTownName(int locX, int locY) {
        MapRegion region = getMapRegion(locX, locY);
        return region == null ? "Aden Castle Town" : region.getTown();
    }
    
    /**
     * Obtiene una región por su nombre.
     * 
     * @param regionName Nombre de la región
     * @return MapRegion si existe, null en caso contrario
     */
    public MapRegion getMapRegionByName(String regionName) {
        return regions.get(regionName);
    }
    
    /**
     * Obtiene el número total de regiones cargadas en memoria.
     * 
     * @return Cantidad total de regiones cargadas
     */
    public int getRegionCount() {
        return regions.size();
    }
    
    /**
     * Recarga completamente todas las regiones desde los archivos XML.
     * 
     * <p>Limpia el catálogo actual y vuelve a cargar todas las regiones desde la ruta
     * configurada. Útil cuando se actualizan los archivos XML sin reiniciar la aplicación.
     */
    public void reload() {
        logger.info("🔄 Recargando regiones desde XMLs...");
        regions.clear();
        loadRegions();
    }
}

