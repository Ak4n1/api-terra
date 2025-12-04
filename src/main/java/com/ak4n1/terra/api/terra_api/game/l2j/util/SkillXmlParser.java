package com.ak4n1.terra.api.terra_api.game.l2j.util;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;
import com.ak4n1.terra.api.terra_api.game.l2j.model.skill.SkillTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser simplificado de archivos XML de skills de L2J.
 * 
 * <p>Esta clase proporciona métodos estáticos para parsear archivos XML que contienen
 * definiciones de skills del juego. Es la clase RECOMENDADA para procesar archivos XML
 * de skills desde SkillTable.
 * 
 * @see com.ak4n1.terra.api.terra_api.game.l2j.data.SkillTable
 * @see SkillTemplate
 * @author ak4n1
 * @since 1.0
 */
public class SkillXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(SkillXmlParser.class);
    
    /**
     * Parsea un archivo XML y retorna un mapa de skills indexados por ID.
     * 
     * <p>Procesa todos los elementos &lt;skill&gt; del archivo XML y crea objetos
     * SkillTemplate. Los skills malformados se registran en los logs pero no detienen
     * el proceso.
     * 
     * @param xmlFile Archivo XML a parsear
     * @return Mapa con los skills parseados indexados por su ID
     */
    public static Map<Integer, SkillTemplate> parseFile(File xmlFile) {
        Map<Integer, SkillTemplate> skills = new HashMap<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList skillNodes = doc.getElementsByTagName("skill");
            
            for (int i = 0; i < skillNodes.getLength(); i++) {
                Node node = skillNodes.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    
                    try {
                        SkillTemplate skill = parseSkillElement(element);
                        if (skill != null) {
                            skills.put(skill.getId(), skill);
                            
                            // Debug para skill 393
                            if (skill.getId() == 393) {
                                logger.info("🔍 [XML PARSER] Skill 393 parsed: {}", skill.toString());
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error parseando skill en archivo {}: {}", xmlFile.getName(), e.getMessage());
                    }
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", xmlFile.getName(), e.getMessage());
        }
        
        return skills;
    }
    
    /**
     * Parsea un XML desde un InputStream y retorna un mapa de skills indexados por ID.
     * 
     * <p>Útil cuando los recursos están dentro de un JAR y no se puede obtener un File directamente.
     * 
     * @param inputStream InputStream del archivo XML a parsear
     * @param fileName Nombre del archivo (para logging)
     * @return Mapa con los skills parseados indexados por su ID
     */
    public static Map<Integer, SkillTemplate> parseFile(InputStream inputStream, String fileName) {
        Map<Integer, SkillTemplate> skills = new HashMap<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();
            
            NodeList skillNodes = doc.getElementsByTagName("skill");
            
            for (int i = 0; i < skillNodes.getLength(); i++) {
                Node node = skillNodes.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    
                    try {
                        SkillTemplate skill = parseSkillElement(element);
                        if (skill != null) {
                            skills.put(skill.getId(), skill);
                            
                            // Debug para skill 393
                            if (skill.getId() == 393) {
                                logger.info("🔍 [XML PARSER] Skill 393 parsed: {}", skill.toString());
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error parseando skill en archivo {}: {}", fileName, e.getMessage());
                    }
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", fileName, e.getMessage());
        }
        
        return skills;
    }
    
    /**
     * Parsea un elemento &lt;skill&gt; individual del XML y crea el SkillTemplate correspondiente.
     * 
     * <p>Extrae todos los atributos necesarios y crea un StatSet que se usa para instanciar
     * el SkillTemplate.
     * 
     * @param skillElement Elemento XML &lt;skill&gt; a parsear
     * @return SkillTemplate parseado, o null si no se pudo parsear
     */
    private static SkillTemplate parseSkillElement(Element skillElement) {
        StatSet set = new StatSet();
        
        // Atributos principales del skill
        int id = Integer.parseInt(skillElement.getAttribute("id").trim());
        String name = skillElement.getAttribute("name").trim();
        String toLevel = skillElement.getAttribute("toLevel").trim();
        
        set.set("skill_id", id);
        set.set("name", name);
        set.set("to_level", Integer.parseInt(toLevel));
        
        // Parsear <icon>
        NodeList iconNodes = skillElement.getElementsByTagName("icon");
        if (iconNodes.getLength() > 0) {
            String icon = iconNodes.item(0).getTextContent().trim();
            set.set("icon", icon);
        }
        
        // Parsear <operateType>
        NodeList operateTypeNodes = skillElement.getElementsByTagName("operateType");
        if (operateTypeNodes.getLength() > 0) {
            String operateType = operateTypeNodes.item(0).getTextContent().trim();
            set.set("operate_type", operateType);
        }
        
        // Parsear <isMagic>
        NodeList isMagicNodes = skillElement.getElementsByTagName("isMagic");
        if (isMagicNodes.getLength() > 0) {
            String isMagicText = isMagicNodes.item(0).getTextContent().trim();
            // Si hay múltiples líneas, tomar solo la primera
            if (isMagicText.contains("\n")) {
                isMagicText = isMagicText.split("\n")[0].trim();
            }
            // Eliminar todos los espacios en blanco restantes
            isMagicText = isMagicText.replaceAll("\\s+", "");
            if (!isMagicText.isEmpty()) {
                int isMagic = Integer.parseInt(isMagicText);
                set.set("is_magic", isMagic);
            }
        }
        
        // Parsear <isDebuff>
        NodeList isDebuffNodes = skillElement.getElementsByTagName("isDebuff");
        if (isDebuffNodes.getLength() > 0) {
            String isDebuffText = isDebuffNodes.item(0).getTextContent().trim();
            // Si hay múltiples líneas, tomar solo la primera
            if (isDebuffText.contains("\n")) {
                isDebuffText = isDebuffText.split("\n")[0].trim();
            }
            // Eliminar todos los espacios en blanco restantes
            isDebuffText = isDebuffText.replaceAll("\\s+", "");
            if (!isDebuffText.isEmpty()) {
                boolean isDebuff = Boolean.parseBoolean(isDebuffText);
                set.set("is_debuff", isDebuff);
            }
        }
        
        // Crear el objeto SkillTemplate
        return new SkillTemplate(set);
    }
}
