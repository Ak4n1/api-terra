package com.ak4n1.terra.api.terra_api.game.l2j.util;

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
 * Parser para archivos XML de experiencia.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class ExperienceXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(ExperienceXmlParser.class);
    
    public static Map<Integer, Long> parseExperienceFile(String filePath) {
        Map<Integer, Long> experienceMap = new HashMap<>();
        
        try {
            File file = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            
            doc.getDocumentElement().normalize();
            
            NodeList experienceNodes = doc.getElementsByTagName("experience");
            
            for (int i = 0; i < experienceNodes.getLength(); i++) {
                Node node = experienceNodes.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    
                    int level = Integer.parseInt(element.getAttribute("level"));
                    long toLevel = Long.parseLong(element.getAttribute("tolevel"));
                    
                    experienceMap.put(level, toLevel);
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("Error parsing experience XML: " + filePath, e);
        }
        
        return experienceMap;
    }
    
    /**
     * Parsea el archivo XML de experiencia desde un InputStream (útil cuando está dentro de un JAR).
     * 
     * @param inputStream InputStream del archivo XML
     * @param fileName Nombre del archivo (para logging)
     * @return Mapa con los niveles de experiencia
     */
    public static Map<Integer, Long> parseExperienceFile(InputStream inputStream, String fileName) {
        Map<Integer, Long> experienceMap = new HashMap<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            
            doc.getDocumentElement().normalize();
            
            NodeList experienceNodes = doc.getElementsByTagName("experience");
            
            for (int i = 0; i < experienceNodes.getLength(); i++) {
                Node node = experienceNodes.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    
                    int level = Integer.parseInt(element.getAttribute("level"));
                    long toLevel = Long.parseLong(element.getAttribute("tolevel"));
                    
                    experienceMap.put(level, toLevel);
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Error parsing experience XML: " + fileName, e);
        }
        
        return experienceMap;
    }
}

