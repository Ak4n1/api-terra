package com.ak4n1.terra.api.terra_api.game.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.ClanWar;
import com.ak4n1.terra.api.terra_api.game.entities.ClanWarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClanWarRepository extends JpaRepository<ClanWar, ClanWarId> {

    @Query("SELECT cw FROM ClanWar cw WHERE cw.clan1 = :clanId OR cw.clan2 = :clanId")
    List<ClanWar> findWarsWhereClanIsClan1OrClan2(@Param("clanId") Integer clanId);

}
