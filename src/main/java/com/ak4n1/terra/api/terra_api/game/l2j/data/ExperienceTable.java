package com.ak4n1.terra.api.terra_api.game.l2j.data;

import com.ak4n1.terra.api.terra_api.game.l2j.util.ExperienceXmlParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tabla de experiencia cargada en memoria.
 * 
 * <p>Carga los datos de experiencia desde el archivo XML al inicio de la aplicación
 * y proporciona métodos para obtener información de experiencia por nivel.
 * Implementa patrón Singleton.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Component
public class ExperienceTable {
    
    private static final Logger logger = LoggerFactory.getLogger(ExperienceTable.class);
    
    // Singleton Holder Pattern (thread-safe lazy initialization)
    private static class SingletonHolder {
        protected static final ExperienceTable INSTANCE = new ExperienceTable();
    }
    
    public static ExperienceTable getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    @Value("${l2j.experience.path:classpath:static/experience/experience.xml}")
    private String experiencePath;
    
    private ResourceLoader resourceLoader;
    private Map<Integer, Long> experienceMap = new HashMap<>();
    private int maxLevel = 107;
    private boolean loaded = false;
    
    // Constructor para Spring (inyección de dependencias)
    public ExperienceTable(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    // Constructor privado para singleton (sin ResourceLoader)
    private ExperienceTable() {
        this.resourceLoader = null;
    }
    
    @PostConstruct
    public void loadExperienceData() {
        loadData();
    }
    
    /**
     * Carga los datos de experiencia desde el archivo XML.
     * 
     * <p>Puede ser llamado múltiples veces de forma thread-safe.
     */
    public synchronized void loadData() {
        if (loaded) {
            return;
        }
        
        try {
            Resource resource;
            String pathToUse = experiencePath != null ? experiencePath : "classpath:static/experience/experience.xml";
            
            // Si tenemos ResourceLoader (Spring bean), usarlo
            if (resourceLoader != null) {
                PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
                resource = resolver.getResource(pathToUse);
            } else {
                // Si no tenemos ResourceLoader (singleton estático), usar ClassPathResource directamente
                String classPathPath = pathToUse.startsWith("classpath:") 
                    ? pathToUse.substring("classpath:".length()) 
                    : pathToUse;
                resource = new ClassPathResource(classPathPath);
            }
            
            if (!resource.exists()) {
                logger.error("❌ Experience XML file not found at: {}", pathToUse);
                return;
            }
            
            logger.info("📂 Loading experience data from: {}", pathToUse);
            
            String fileName = resource.getFilename() != null ? resource.getFilename() : "experience.xml";
            
            // Intentar usar File si está disponible (desarrollo)
            try {
                File xmlFile = resource.getFile();
                experienceMap = ExperienceXmlParser.parseExperienceFile(xmlFile.getAbsolutePath());
            } catch (Exception e) {
                // Si está dentro del JAR, usar InputStream (producción)
                try (InputStream inputStream = resource.getInputStream()) {
                    experienceMap = ExperienceXmlParser.parseExperienceFile(inputStream, fileName);
                }
            }
            
            if (!experienceMap.isEmpty()) {
                maxLevel = experienceMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(107);
                logger.info("✅ Loaded {} experience levels, max level: {}", experienceMap.size(), maxLevel);
            }
            
            loaded = true;
            
        } catch (Exception e) {
            logger.error("❌ Error loading experience data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene el exp necesario para alcanzar un nivel específico.
     * 
     * @param level Nivel a consultar
     * @return Exp necesario para alcanzar ese nivel, o 0 si no existe
     */
    public long getExpForLevel(int level) {
        if (!loaded) {
            loadData();
        }
        return experienceMap.getOrDefault(level, 0L);
    }
    
    /**
     * Calcula el porcentaje de experiencia actual del personaje.
     * 
     * @param currentExp Exp actual del personaje
     * @param currentLevel Nivel actual del personaje
     * @return Porcentaje formateado (ej: "65,52%") o "0,00%" si hay error
     */
    public String calculateExpPercent(long currentExp, int currentLevel) {
        try {
            // Obtener exp necesario para el nivel actual
            long expCurrentLevel = getExpForLevel(currentLevel);
            
            // Obtener exp necesario para el siguiente nivel
            int nextLevel = currentLevel + 1;
            long expNextLevel = getExpForLevel(nextLevel);
            
            // Si no hay siguiente nivel (nivel máximo), retornar 100%
            if (expNextLevel == 0 || currentLevel >= maxLevel) {
                return "100,00%";
            }
            
            // Calcular diferencia de exp entre niveles
            long expDiff = expNextLevel - expCurrentLevel;
            
            if (expDiff <= 0) {
                return "0,00%";
            }
            
            // Calcular exp actual en el nivel actual
            long expInCurrentLevel = currentExp - expCurrentLevel;
            
            // Calcular porcentaje
            double percent = ((double) expInCurrentLevel / (double) expDiff) * 100.0;
            
            // Formatear con coma como separador decimal y 2 decimales
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("0.00", symbols);
            
            return df.format(percent) + "%";
            
        } catch (Exception e) {
            logger.error("Error calculating exp percent: {}", e.getMessage());
            return "0,00%";
        }
    }
    
    /**
     * Obtiene el nivel máximo disponible.
     * 
     * @return Nivel máximo
     */
    public int getMaxLevel() {
        if (!loaded) {
            loadData();
        }
        return maxLevel;
    }
}
