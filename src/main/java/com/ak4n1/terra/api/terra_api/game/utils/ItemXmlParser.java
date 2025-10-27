package com.ak4n1.terra.api.terra_api.game.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ItemXmlParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemXmlParser.class);

    // En ItemXmlParser.java
    public static Map<Integer, ItemData> parse(File xmlFile) {
        Map<Integer, ItemData> items = new HashMap<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList itemNodes = document.getElementsByTagName("item");

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element itemElement = (Element) itemNodes.item(i);

                int id;
                try {
                    id = Integer.parseInt(itemElement.getAttribute("id"));
                } catch (NumberFormatException e) {
                    logger.warn("ID inválido en archivo {} índice {}: '{}'", xmlFile.getName(), i, itemElement.getAttribute("id"));
                    continue;
                }

                String name = itemElement.getAttribute("name");
                String type = itemElement.getAttribute("type");

                // Verificar si ya existe un item con información más completa
                ItemData existingItem = items.get(id);
                if (existingItem != null) {
                    // Si el item existente tiene nombre y tipo, y el actual no, saltar
                    if (!existingItem.getName().isEmpty() && !existingItem.getType().isEmpty() && 
                        (name == null || name.isEmpty() || type == null || type.isEmpty())) {
                        logger.debug("⏭️ Saltando item {} en {} (ya existe versión completa)", id, xmlFile.getName());
                        continue;
                    }
                    // Si el actual tiene información más completa, reemplazar
                    if ((name != null && !name.isEmpty() && type != null && !type.isEmpty()) &&
                        (existingItem.getName().isEmpty() || existingItem.getType().isEmpty())) {
                        logger.debug("🔄 Reemplazando item {} en {} con versión más completa", id, xmlFile.getName());
                        items.remove(id);
                    } else {
                        // Si ambos tienen información similar, mantener el existente
                        continue;
                    }
                }

                if (name == null || name.isEmpty() || type == null || type.isEmpty()) {
                    logger.warn("⚠️ Item ID {} con nombre o tipo vacío en archivo {}. name='{}', type='{}'", id, xmlFile.getName(), name, type);
                }

                ItemData item = new ItemData(id, name != null ? name : "", type != null ? type : "");

                NodeList setNodes = itemElement.getElementsByTagName("set");
                for (int j = 0; j < setNodes.getLength(); j++) {
                    Element setElement = (Element) setNodes.item(j);
                    String attrName = setElement.getAttribute("name");
                    String attrValue = setElement.getAttribute("val");
                    if (attrName != null && !attrName.isEmpty() && attrValue != null) {
                        item.addAttribute(attrName, attrValue);
                        
                        // Log específico para el item 29520
                        if (id == 29520) {
                            logger.info("🔧 DEBUG 29520 - Agregando atributo: {} = {}", attrName, attrValue);
                        }
                    } else {
                        // Log para atributos malformados
                        if (id == 29520) {
                            logger.warn("⚠️ DEBUG 29520 - Atributo malformado: name='{}', val='{}'", attrName, attrValue);
                        }
                    }
                }

                NodeList statsNodes = itemElement.getElementsByTagName("stats");
                if (statsNodes.getLength() > 0) {
                    Element statsElement = (Element) statsNodes.item(0);
                    NodeList statNodes = statsElement.getElementsByTagName("stat");

                    for (int k = 0; k < statNodes.getLength(); k++) {
                        Element statElement = (Element) statNodes.item(k);
                        String statType = statElement.getAttribute("type");
                        String statText = statElement.getTextContent().trim();

                        try {
                            int statValue = (int) Math.round(Double.parseDouble(statText));
                            item.addStat(statType, statValue);
                        } catch (NumberFormatException e) {
                            logger.warn("⚠️ Stat no numérico para '{}' en item ID {}: '{}'", statType, id, statText);
                        }
                    }
                }

                // Log completo de ítems con datos vacíos
                if (id == 29520) {
                    logger.warn("🧪 DEBUG 29520 - name='{}', type='{}', attrs={}, stats={}, archivo={}",
                            name, type, item.getAttributes(), item.getStats(), xmlFile.getName());
                }

                items.put(id, item);
            }

            logger.info("📁 Parseados {} ítems desde: {}", items.size(), xmlFile.getName());

        } catch (Exception e) {
            logger.error("❌ Error parseando XML {}: {}", xmlFile.getName(), e.toString());
        }

        return items;
    }

}
