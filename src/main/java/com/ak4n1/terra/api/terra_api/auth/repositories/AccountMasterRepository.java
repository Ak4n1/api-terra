package com.ak4n1.terra.api.terra_api.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;

import java.util.Optional;

public interface AccountMasterRepository extends JpaRepository<AccountMaster, Long> {

    Optional<AccountMaster> findByVerificationToken(String verificationToken);
    boolean existsByEmail(String email);
    Optional<AccountMaster> findByEmail(String email);
    Optional<AccountMaster> findByPasswordResetToken(String tokenUser);

}
