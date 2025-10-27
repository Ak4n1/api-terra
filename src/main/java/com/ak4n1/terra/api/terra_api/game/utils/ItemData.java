package com.ak4n1.terra.api.terra_api.game.utils;

import java.util.HashMap;
import java.util.Map;

public class ItemData {
    private int id;
    private String name;
    private String type;
    private Map<String, String> attributes = new HashMap<>();
    private Map<String, Integer> stats = new HashMap<>();

    public ItemData() {}

    public ItemData(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public Map<String, Integer> getStats() { return stats; }
    public void setStats(Map<String, Integer> stats) { this.stats = stats; }

    // Métodos de utilidad para agregar atributos y stats
    public void addAttribute(String name, String value) { this.attributes.put(name, value); }
    public void addStat(String type, int value) { this.stats.put(type, value); }

    public String getAttribute(String name) { return attributes.get(name); }
    public Integer getStat(String type) { return stats.get(type); }

    @Override
    public String toString() {
        return "ItemData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", attributes=" + attributes.size() +
                ", stats=" + stats.size() +
                '}';
    }
}
