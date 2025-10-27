package com.ak4n1.terra.api.terra_api.payments.dto;

import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import java.math.BigDecimal;

/**
 * DTO de respuesta para paquetes de monedas
 */
public class CoinPackageResponseDTO {
    
    private Long id;
    private String name;
    private Integer coinsAmount;
    private Integer bonusCoins;
    private Integer totalCoins;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer bonusPercentage;
    private String description;
    private boolean popular;
    private boolean active;
    private BigDecimal discountAmount;
    private Integer discountPercentage;
    
    // Constructor por defecto
    public CoinPackageResponseDTO() {}
    
    // Constructor desde entidad
    public CoinPackageResponseDTO(CoinPackage coinPackage) {
        this.id = coinPackage.getId();
        this.name = coinPackage.getName();
        this.coinsAmount = coinPackage.getCoinsAmount();
        this.bonusCoins = coinPackage.getBonusCoins();
        this.totalCoins = coinPackage.getTotalCoins();
        this.price = coinPackage.getPrice();
        this.originalPrice = coinPackage.getOriginalPrice();
        this.bonusPercentage = coinPackage.getBonusPercentage();
        this.description = coinPackage.getDescription();
        this.popular = coinPackage.isPopular();
        this.active = coinPackage.isActive();
        this.discountAmount = coinPackage.getDiscountAmount();
        this.discountPercentage = coinPackage.getDiscountPercentage();
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
    
    public Integer getTotalCoins() {
        return totalCoins;
    }
    
    public void setTotalCoins(Integer totalCoins) {
        this.totalCoins = totalCoins;
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
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public Integer getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
