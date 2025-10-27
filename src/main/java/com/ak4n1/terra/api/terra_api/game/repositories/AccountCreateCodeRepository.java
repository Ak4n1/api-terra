package com.ak4n1.terra.api.terra_api.game.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.AccountCreateCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountCreateCodeRepository extends JpaRepository<AccountCreateCode, Long> {


    Optional<AccountCreateCode> findByEmail(String email);

    // Podés agregar más consultas si necesitás
}
