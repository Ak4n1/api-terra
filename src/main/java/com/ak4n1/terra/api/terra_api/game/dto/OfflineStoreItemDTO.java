package com.ak4n1.terra.api.terra_api.game.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OfflineStoreItemDTO {
    private int itemId;
    private long count;
    private long price;

    private Integer enchantLevel;
    private Long time;

    private String name;
    private String type;

    // Campos crudos para transformar desde texto a JSON
    private String rawAttributes;
    private String rawStats;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Constructor vacío por claridad
    public OfflineStoreItemDTO() {}

    // Setters
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setCount(long count) { this.count = count; }
    public void setPrice(long price) { this.price = price; }
    public void setEnchantLevel(Integer enchantLevel) { this.enchantLevel = enchantLevel; }
    public void setTime(Long time) { this.time = time; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setAttributes(String rawAttributes) { this.rawAttributes = rawAttributes; }
    public void setStats(String rawStats) { this.rawStats = rawStats; }

    // Getters
    public int getItemId() { return itemId; }
    public long getCount() { return count; }
    public long getPrice() { return price; }
    public Integer getEnchantLevel() { return enchantLevel; }
    public Long getTime() { return time; }
    public String getName() { return name; }
    public String getType() { return type; }

    public String getRawAttributes() {
        return rawAttributes;
    }

    public void setRawAttributes(String rawAttributes) {
        this.rawAttributes = rawAttributes;
    }

    public String getRawStats() {
        return rawStats;
    }

    public void setRawStats(String rawStats) {
        this.rawStats = rawStats;
    }

    // JSON resultante para frontend
    public Map<String, Object> getAttributes() {
        return parseMapString(rawAttributes);
    }

    public Map<String, Object> getStats() {
        return parseMapString(rawStats);
    }

    // Conversor robusto texto → Map JSON
    private Map<String, Object> parseMapString(String raw) {
        if (raw == null || raw.isEmpty()) return Collections.emptyMap();

        try {
            String json = raw.trim()
                    .replaceAll("([\\{,]\\s*)([^\\{\\}\\[\\],:=]+)=", "$1\"$2\":")
                    .replaceAll("=([^\\{\\}\\[\\],:=]+)([,}])", ":\"$1\"$2")
                    .replaceAll(",\\s*}", "}");

            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
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
