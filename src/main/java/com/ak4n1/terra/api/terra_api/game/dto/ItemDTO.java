package com.ak4n1.terra.api.terra_api.game.dto;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemDTO {
    private int objectId;
    private int itemId;
    private long count;
    private int enchantLevel;
    private String location;
    private int locationData;
    private String name = "item";
    private String type;
    private String player;





    // Campos crudos
    private String rawAttributes;
    private String rawStats;

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ItemDTO() {}

    // Getters y setters básicos
    public int getObjectId() { return objectId; }
    public void setObjectId(int objectId) { this.objectId = objectId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public int getEnchantLevel() { return enchantLevel; }
    public void setEnchantLevel(int enchantLevel) { this.enchantLevel = enchantLevel; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getLocationData() { return locationData; }
    public void setLocationData(int locationData) { this.locationData = locationData; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public void setRawAttributes(String rawAttributes) { this.rawAttributes = rawAttributes; }
    public void setRawStats(String rawStats) { this.rawStats = rawStats; }

    // Métodos para exponer como JSON automáticamente
    public Map<String, Object> getAttributes() {
        return parseMapString(rawAttributes);
    }

    public Map<String, Object> getStats() {
        return parseMapString(rawStats);
    }

    // Utilidad robusta para parsear = a JSON
    private Map<String, Object> parseMapString(String raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyMap();

        try {
            // Intenta convertir = a JSON válido
            String json = raw.trim()
                    .replaceAll("([\\{,]\\s*)([^\\{\\}\\[\\],:=]+)=", "$1\"$2\":")
                    .replaceAll("=([^\\{\\}\\[\\],:=]+)([,}])", ":\"$1\"$2")
                    .replaceAll(",\\s*}", "}");

            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            // Fallback simple a mano
            Map<String, Object> map = new HashMap<>();
            String cleaned = raw.replaceAll("^\\{", "").replaceAll("}$", "");
            for (String pair : cleaned.split(",")) {
                String[] kv = pair.trim().split("=");
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                }
            }
            return map;
        }
    }


}
