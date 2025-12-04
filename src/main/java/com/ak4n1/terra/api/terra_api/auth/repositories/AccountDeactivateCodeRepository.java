package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountDeactivateCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountDeactivateCodeRepository extends JpaRepository<AccountDeactivateCode, Long> {
    Optional<AccountDeactivateCode> findByEmail(String email);
}

