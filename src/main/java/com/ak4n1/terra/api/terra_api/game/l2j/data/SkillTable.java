package com.ak4n1.terra.api.terra_api.game.l2j.data;

import com.ak4n1.terra.api.terra_api.game.l2j.model.skill.SkillTemplate;
import com.ak4n1.terra.api.terra_api.game.l2j.util.SkillXmlParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Tabla de skills simplificada del core L2J.
 * 
 * <p>Componente principal que carga todos los skills desde archivos XML en memoria
 * al iniciar la aplicación. Proporciona búsqueda O(1) por ID mediante un mapa indexado.
 * Es la clase RECOMENDADA para acceder al catálogo de skills desde cualquier parte
 * de la aplicación.
 * 
 * <p>Características:
 * <ul>
 *   <li>Carga automática al iniciar Spring Boot mediante @PostConstruct</li>
 *   <li>Búsqueda O(1) por ID usando HashMap</li>
 *   <li>Recarga manual disponible</li>
 * </ul>
 * 
 * @see SkillTemplate
 * @see SkillXmlParser
 * @author ak4n1
 * @since 1.0
 */
@Component
public class SkillTable {
    
    private static final Logger logger = LoggerFactory.getLogger(SkillTable.class);
    
    @Value("${l2j.skills.path:classpath:static/skills}")
    private String skillsPath;
    
    private final ResourceLoader resourceLoader;
    private final Map<Integer, SkillTemplate> _skillsMap = new HashMap<>();
    
    public SkillTable(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Inicializa y carga todos los skills desde los archivos XML.
     * 
     * <p>Este método se ejecuta automáticamente al iniciar Spring Boot mediante
     * @PostConstruct. Lee todos los archivos XML de la ruta configurada y carga
     * los skills en memoria.
     */
    @PostConstruct
    public void init() {
        logger.info("🔄 Iniciando carga de skills desde XMLs...");
        logger.info("📂 Ruta configurada: {}", skillsPath);
        loadSkills();
    }
    
    /**
     * Carga todos los skills desde los archivos XML de la ruta configurada.
     * 
     * <p>Procesa todos los archivos XML encontrados en el directorio, parsea cada
     * skill y los almacena en un mapa.
     */
    private void loadSkills() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
            String pattern = skillsPath.startsWith("classpath:") 
                ? skillsPath.replace("classpath:", "classpath:") + "/*.xml"
                : skillsPath + "/*.xml";
            
            Resource[] resources = resolver.getResources(pattern);
            
            if (resources.length == 0) {
                logger.warn("⚠️ No se encontraron archivos XML en: {}", skillsPath);
                return;
            }
            
            logger.info("📁 Encontrados {} archivos XML para procesar", resources.length);
            
            int totalSkills = 0;
            
            // Parsear todos los archivos
            for (Resource resource : resources) {
                try {
                    String fileName = resource.getFilename() != null ? resource.getFilename() : "unknown.xml";
                    Map<Integer, SkillTemplate> parsedSkills;
                    
                    // Intentar usar File si está disponible (desarrollo)
                    try {
                        File xmlFile = resource.getFile();
                        parsedSkills = SkillXmlParser.parseFile(xmlFile);
                    } catch (IOException e) {
                        // Si está dentro del JAR, usar InputStream (producción)
                        try (InputStream inputStream = resource.getInputStream()) {
                            parsedSkills = SkillXmlParser.parseFile(inputStream, fileName);
                        }
                    }
                    
                    for (Map.Entry<Integer, SkillTemplate> entry : parsedSkills.entrySet()) {
                        int skillId = entry.getKey();
                        SkillTemplate skill = entry.getValue();
                        
                        // Guardar en el mapa
                        _skillsMap.put(skillId, skill);
                        totalSkills++;
                    }
                } catch (Exception e) {
                    String fileName = resource.getFilename() != null ? resource.getFilename() : "unknown.xml";
                    logger.error("Error procesando archivo {}: {}", fileName, e.getMessage());
                }
            }
            
            logger.info("✅ Carga completada: {} skills cargados en memoria", totalSkills);
        } catch (Exception e) {
            logger.error("❌ Error al cargar skills: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene un template de skill por su ID.
     * 
     * <p>Búsqueda O(1) usando HashMap. Retorna null si el skill no existe.
     * 
     * @param skillId ID del skill
     * @return SkillTemplate si existe, null en caso contrario
     */
    public SkillTemplate getTemplate(int skillId) {
        return _skillsMap.get(skillId);
    }
    
    /**
     * Obtiene todos los templates de skills cargados.
     * 
     * @return Colección con todos los SkillTemplate
     */
    public Collection<SkillTemplate> getAllTemplates() {
        return _skillsMap.values();
    }
    
    /**
     * Retorna el número total de skills cargados.
     * 
     * @return Número total de skills en el catálogo
     */
    public int getSkillCount() {
        return _skillsMap.size();
    }
    
    /**
     * Recarga todos los skills desde los archivos XML.
     * 
     * <p>Útil para actualizar el catálogo sin reiniciar la aplicación.
     * Limpia el mapa actual y vuelve a cargar desde los archivos XML.
     */
    public void reload() {
        logger.info("🔄 Recargando catálogo de skills...");
        _skillsMap.clear();
        loadSkills();
    }
}
