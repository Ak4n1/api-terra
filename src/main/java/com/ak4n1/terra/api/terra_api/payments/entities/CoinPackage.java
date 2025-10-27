package com.ak4n1.terra.api.terra_api.payments.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad para paquetes de monedas disponibles para compra
 */
@Entity
@Table(name = "coin_packages")
public class CoinPackage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name; // "Pack Básico", "Pack Premium", etc.
    
    @Column(name = "coins_amount", nullable = false)
    private Integer coinsAmount; // Monedas base del paquete
    
    @Column(name = "bonus_coins", nullable = false)
    private Integer bonusCoins = 0; // Monedas extra por bonus
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Precio final
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice; // Precio sin descuento (opcional)
    
    @Column(name = "bonus_percentage", nullable = false)
    private Integer bonusPercentage = 0; // Porcentaje de bonus (0-100)
    
    @Column(name = "description", length = 255)
    private String description; // "300 Terra Coins + 30 Bonus"
    
    @Column(name = "popular", nullable = false)
    private boolean popular = false; // Badge "Más Popular"
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // Orden de visualización
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;
    
    // Constructor
    public CoinPackage() {
        this.createdAt = new java.util.Date();
    }
    
    // Método para obtener total de monedas (base + bonus)
    @Transient
    public Integer getTotalCoins() {
        return coinsAmount + bonusCoins;
    }
    
    // Método para calcular descuento
    @Transient
    public BigDecimal getDiscountAmount() {
        if (originalPrice != null && originalPrice.compareTo(price) > 0) {
            return originalPrice.subtract(price);
        }
        return BigDecimal.ZERO;
    }
    
    // Método para calcular porcentaje de descuento
    @Transient
    public Integer getDiscountPercentage() {
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = originalPrice.subtract(price);
            return discount.multiply(BigDecimal.valueOf(100))
                          .divide(originalPrice, 0, BigDecimal.ROUND_HALF_UP)
                          .intValue();
        }
        return 0;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getCoinsAmount() {
        return coinsAmount;
    }
    
    public void setCoinsAmount(Integer coinsAmount) {
        this.coinsAmount = coinsAmount;
    }
    
    public Integer getBonusCoins() {
        return bonusCoins;
    }
    
    public void setBonusCoins(Integer bonusCoins) {
        this.bonusCoins = bonusCoins;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Integer getBonusPercentage() {
        return bonusPercentage;
    }
    
    public void setBonusPercentage(Integer bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isPopular() {
        return popular;
    }
    
    public void setPopular(boolean popular) {
        this.popular = popular;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public java.util.Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public java.util.Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new java.util.Date();
    }
}
