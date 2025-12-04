package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountDeactivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountDeactivationRepository extends JpaRepository<AccountDeactivation, Long> {
    
    /**
     * Busca la última desactivación de una cuenta.
     * 
     * @param accountMasterId ID de la cuenta
     * @return La última desactivación si existe
     */
    Optional<AccountDeactivation> findFirstByAccountMasterIdOrderByDeactivationDateDesc(Long accountMasterId);
    
    /**
     * Busca todas las desactivaciones de una cuenta.
     * 
     * @param accountMasterId ID de la cuenta
     * @return Lista de desactivaciones ordenadas por fecha descendente
     */
    List<AccountDeactivation> findByAccountMasterIdOrderByDeactivationDateDesc(Long accountMasterId);
}

