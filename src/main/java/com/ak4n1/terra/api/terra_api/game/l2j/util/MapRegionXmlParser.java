package com.ak4n1.terra.api.terra_api.game.l2j.util;

import com.ak4n1.terra.api.terra_api.game.l2j.model.mapregion.MapRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser simplificado de archivos XML de regiones del mapa de L2J.
 * 
 * <p>Esta clase proporciona métodos estáticos para parsear archivos XML que contienen
 * definiciones de regiones del mapa del juego. La lógica es adaptada del parseDocument
 * del MapRegionManager del core L2J Mobius Classic 3.0.
 * 
 * <p>Es la clase RECOMENDADA para procesar archivos XML de regiones desde MapRegionTable.
 * 
 * @see MapRegionTable
 * @see MapRegion
 * @author ak4n1
 * @since 1.0
 */
public class MapRegionXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(MapRegionXmlParser.class);
    
    /**
     * Parsea un archivo XML y retorna una lista de regiones.
     * 
     * <p>Procesa todos los elementos &lt;region&gt; del archivo XML y crea objetos
     * MapRegion. La lógica es adaptada del parseDocument del MapRegionManager del core.
     * Las regiones malformadas se registran en los logs pero no detienen el proceso.
     * 
     * @param xmlFile Archivo XML a parsear
     * @return Lista con las regiones parseadas
     */
    public static List<MapRegion> parseFile(File xmlFile) {
        List<MapRegion> regions = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            // Lógica adaptada del parseDocument del MapRegionManager del core
            NamedNodeMap attrs;
            String name;
            String town;
            int locId;
            
            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("region".equalsIgnoreCase(d.getNodeName())) {
                            attrs = d.getAttributes();
                            name = attrs.getNamedItem("name").getNodeValue();
                            town = attrs.getNamedItem("town").getNodeValue();
                            locId = parseInteger(attrs, "locId");
                            
                            final MapRegion region = new MapRegion(name, town, locId);
                            
                            // Parsear elementos hijos (map, respawnPoint, banned, etc.)
                            for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                                attrs = c.getAttributes();
                                if ("map".equalsIgnoreCase(c.getNodeName())) {
                                    region.addMap(parseInteger(attrs, "X"), parseInteger(attrs, "Y"));
                                }
                                // Nota: No parseamos respawnPoint ni banned ya que no los necesitamos para la API
                            }
                            
                            regions.add(region);
                        }
                    }
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", xmlFile.getName(), e.getMessage(), e);
        }
        
        return regions;
    }
    
    /**
     * Parsea un XML desde un InputStream y retorna una lista de regiones.
     * 
     * <p>Útil cuando los recursos están dentro de un JAR y no se puede obtener un File directamente.
     * 
     * @param inputStream InputStream del archivo XML a parsear
     * @param fileName Nombre del archivo (para logging)
     * @return Lista con las regiones parseadas
     */
    public static List<MapRegion> parseFile(InputStream inputStream, String fileName) {
        List<MapRegion> regions = new ArrayList<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();
            
            // Lógica adaptada del parseDocument del MapRegionManager del core
            NamedNodeMap attrs;
            String name;
            String town;
            int locId;
            
            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("region".equalsIgnoreCase(d.getNodeName())) {
                            attrs = d.getAttributes();
                            name = attrs.getNamedItem("name").getNodeValue();
                            town = attrs.getNamedItem("town").getNodeValue();
                            locId = parseInteger(attrs, "locId");
                            
                            final MapRegion region = new MapRegion(name, town, locId);
                            
                            // Parsear elementos hijos (map, respawnPoint, banned, etc.)
                            for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                                attrs = c.getAttributes();
                                if ("map".equalsIgnoreCase(c.getNodeName())) {
                                    region.addMap(parseInteger(attrs, "X"), parseInteger(attrs, "Y"));
                                }
                                // Nota: No parseamos respawnPoint ni banned ya que no los necesitamos para la API
                            }
                            
                            regions.add(region);
                        }
                    }
                }
            }
            
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", fileName, e.getMessage(), e);
        }
        
        return regions;
    }
    
    /**
     * Parsea un entero desde un NamedNodeMap.
     * 
     * @param attrs NamedNodeMap con los atributos
     * @param name Nombre del atributo
     * @return Valor entero parseado, o 0 si no existe o hay error
     */
    private static int parseInteger(NamedNodeMap attrs, String name) {
        Node node = attrs.getNamedItem(name);
        if (node != null) {
            try {
                return Integer.parseInt(node.getNodeValue());
            } catch (NumberFormatException e) {
                logger.warn("Error parseando entero para atributo {}: {}", name, node.getNodeValue());
            }
        }
        return 0;
    }
}

