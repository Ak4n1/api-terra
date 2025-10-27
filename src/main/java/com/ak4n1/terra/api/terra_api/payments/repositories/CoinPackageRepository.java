package com.ak4n1.terra.api.terra_api.payments.repositories;

import com.ak4n1.terra.api.terra_api.payments.entities.CoinPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para paquetes de monedas
 */
@Repository
public interface CoinPackageRepository extends JpaRepository<CoinPackage, Long> {
    
    /**
     * Buscar paquetes activos ordenados por sort_order
     */
    @Query("SELECT cp FROM CoinPackage cp WHERE cp.active = true ORDER BY cp.sortOrder ASC")
    List<CoinPackage> findActivePackagesOrdered();
    
    /**
     * Buscar paquetes activos
     */
    List<CoinPackage> findByActiveTrue();
    
    /**
     * Buscar paquetes activos ordenados por sort_order
     */
    List<CoinPackage> findByActiveTrueOrderBySortOrderAsc();
    
    /**
     * Buscar paquete por ID y que esté activo
     */
    Optional<CoinPackage> findByIdAndActiveTrue(Long id);
    
    /**
     * Buscar paquetes populares
     */
    List<CoinPackage> findByPopularTrueAndActiveTrue();
    
    /**
     * Contar paquetes activos
     */
    long countByActiveTrue();
}
