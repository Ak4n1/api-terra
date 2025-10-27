package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveTokenRepository extends CrudRepository<ActiveToken, Long> {


    Optional<ActiveToken> findByToken(String token);  // Método para buscar un ActiveToken por el token
    
    List<ActiveToken> findByAccountMaster_Email(String email);  // Método para buscar tokens por email del usuario



    @Transactional
    @Modifying
    @Query("DELETE FROM ActiveToken a WHERE a.accountMaster.id = :userId")
    void deleteOldTokensByUserId(@Param("userId") Long userId);

    void deleteByToken(String token);

}
