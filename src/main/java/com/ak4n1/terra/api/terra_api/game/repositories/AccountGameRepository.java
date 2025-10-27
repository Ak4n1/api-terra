package com.ak4n1.terra.api.terra_api.game.repositories;


import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AccountGameRepository extends JpaRepository<AccountGame, String> {
    List<AccountGame> findByEmail(String email);
    Optional<AccountGame> findByLogin(String login);
    boolean existsByLogin(String login);



}
