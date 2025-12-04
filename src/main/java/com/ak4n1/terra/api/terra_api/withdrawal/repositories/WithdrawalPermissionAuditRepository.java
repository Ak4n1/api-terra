package com.ak4n1.terra.api.terra_api.withdrawal.repositories;

import com.ak4n1.terra.api.terra_api.withdrawal.entities.WithdrawalPermissionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para auditoría de permisos de retiro.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface WithdrawalPermissionAuditRepository extends JpaRepository<WithdrawalPermissionAudit, Long> {

    /**
     * Busca todos los registros de auditoría para un email de cuenta.
     * 
     * @param accountEmail Email del account master
     * @return Lista de registros de auditoría ordenados por fecha
     */
    List<WithdrawalPermissionAudit> findByAccountEmailOrderByCreatedAtDesc(String accountEmail);

    /**
     * Busca todos los registros de auditoría para un personaje específico.
     * 
     * @param characterId ID del personaje
     * @return Lista de registros de auditoría ordenados por fecha
     */
    List<WithdrawalPermissionAudit> findByCharacterIdOrderByCreatedAtDesc(Integer characterId);
}

