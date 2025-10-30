package com.ak4n1.terra.api.terra_api.game.l2j.util;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.Armor;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.EtcItem;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.Weapon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser simplificado de archivos XML de items de L2J.
 * 
 * <p>Esta clase proporciona métodos estáticos para parsear archivos XML que contienen
 * definiciones de items del juego. Soportará los tipos: Weapon, Armor y EtcItem.
 * Es la clase RECOMENDADA para procesar archivos XML de items desde ItemTable.
 * 
 * @see ItemTable
 * @see ItemTemplate
 * @see Weapon
 * @see Armor
 * @see EtcItem
 * @author ak4n1
 * @since 1.0
 */
public class ItemXmlParser {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemXmlParser.class);
    
    /**
     * Parsea un archivo XML y retorna un mapa de items indexados por ID.
     * 
     * <p>Procesa todos los elementos &lt;item&gt; del archivo XML y crea objetos
     * ItemTemplate según el tipo especificado. Los items malformados se registran
     * en los logs pero no detienen el proceso.
     * 
     * @param xmlFile Archivo XML a parsear
     * @return Mapa con los items parseados indexados por su ID
     */
    public static Map<Integer, ItemTemplate> parseFile(File xmlFile) {
        Map<Integer, ItemTemplate> items = new HashMap<>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList itemNodes = doc.getElementsByTagName("item");
            
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node node = itemNodes.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    
                    try {
                        ItemTemplate item = parseItemElement(element);
                        if (item != null) {
                            items.put(item.getId(), item);
                        }
                    } catch (Exception e) {
                        logger.warn("Error parseando item en archivo {}: {}", xmlFile.getName(), e.getMessage());
                    }
                }
            }
            
            logger.debug("Parseados {} items del archivo {}", items.size(), xmlFile.getName());
            
        } catch (Exception e) {
            logger.error("Error parseando archivo XML {}: {}", xmlFile.getName(), e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Parsea un elemento &lt;item&gt; individual del XML y crea el ItemTemplate correspondiente.
     * 
     * <p>Extrae todos los atributos y elementos &lt;set&gt; y &lt;stat&gt;, creando un
     * StatSet que se usa para instanciar el tipo correcto de ItemTemplate (Weapon, Armor o EtcItem).
     * 
     * @param itemElement Elemento XML &lt;item&gt; a parsear
     * @return ItemTemplate parseado según el tipo, o null si el tipo es desconocido
     */
    private static ItemTemplate parseItemElement(Element itemElement) {
        StatSet set = new StatSet();
        
        // Atributos principales del item
        int id = Integer.parseInt(itemElement.getAttribute("id"));
        String name = itemElement.getAttribute("name");
        String type = itemElement.getAttribute("type");
        
        set.set("item_id", id);
        set.set("name", name);
        
        // Parsear todos los elementos <set>
        NodeList setNodes = itemElement.getElementsByTagName("set");
        for (int i = 0; i < setNodes.getLength(); i++) {
            Node setNode = setNodes.item(i);
            if (setNode.getNodeType() == Node.ELEMENT_NODE) {
                Element setElement = (Element) setNode;
                String setName = setElement.getAttribute("name");
                String setVal = setElement.getAttribute("val");
                
                // Intentar parsear como número si es posible
                try {
                    if (setVal.contains(".")) {
                        set.set(setName, Double.parseDouble(setVal));
                    } else {
                        set.set(setName, Integer.parseInt(setVal));
                    }
                } catch (NumberFormatException e) {
                    // Si no es número, guardar como string
                    set.set(setName, setVal);
                }
            }
        }
        
        // Parsear stats si existen
        NodeList statsNodes = itemElement.getElementsByTagName("stats");
        if (statsNodes.getLength() > 0) {
            Element statsElement = (Element) statsNodes.item(0);
            NodeList statNodes = statsElement.getElementsByTagName("stat");
            
            for (int i = 0; i < statNodes.getLength(); i++) {
                Node statNode = statNodes.item(i);
                if (statNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element statElement = (Element) statNode;
                    String statType = statElement.getAttribute("type");
                    String statValue = statElement.getTextContent();
                    
                    try {
                        set.set(statType, Integer.parseInt(statValue));
                    } catch (NumberFormatException e) {
                        set.set(statType, statValue);
                    }
                }
            }
        }
        
        // Crear el objeto según el tipo
        switch (type) {
            case "Weapon":
                return new Weapon(set);
            case "Armor":
                return new Armor(set);
            case "EtcItem":
                return new EtcItem(set);
            default:
                logger.warn("Tipo de item desconocido: {} para item {}", type, id);
                return null;
        }
    }
}

