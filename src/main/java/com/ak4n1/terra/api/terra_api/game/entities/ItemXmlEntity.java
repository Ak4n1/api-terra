package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "item_xml")
public class ItemXmlEntity {

    @Id
    private int id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String attributes;  // Guardamos el Map<String, String> como String

    @Column(columnDefinition = "TEXT")
    private String stats;       // Guardamos el Map<String, Integer> como String

    // Constructor vacío necesario para JPA
    public ItemXmlEntity() {}

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }
}