package com.ak4n1.terra.api.terra_api.game.services;


import com.ak4n1.terra.api.terra_api.game.entities.Clan;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ClanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ak4n1.terra.api.terra_api.game.dto.ClanRanking;
import com.ak4n1.terra.api.terra_api.game.dto.PlayerRanking;
import com.ak4n1.terra.api.terra_api.game.dto.RankingClanResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPKResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPvPResponseDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de rankings del juego.
 * 
 * <p>Este servicio proporciona acceso a los rankings de PK, PvP y clanes,
 * siempre retornando los top 10 jugadores/clanes ordenados según el criterio
 * correspondiente. Si hay menos de 10 entradas, se completan con valores vacíos.
 * 
 * @see RankingsService
 * @see CharacterRepository
 * @see ClanRepository
 * @author ak4n1
 * @since 1.0
 */
@Service
public class RankingsServiceImpl implements RankingsService {

    @Autowired
    private CharacterRepository characterRepository;


    @Autowired
    private ClanRepository clanRepository;

    /**
     * {@inheritDoc}
     * 
     * @return DTO con el ranking de top 10 PK
     */
    @Override
    public RankingPKResponseDTO getTopPk() {
        // Traigo el top de la DB (puede tener menos de 10)
        List<Character> topPk = this.characterRepository.findTop10ByPkkillsOrderByPkkillsDesc();

        // DTO para la respuesta
        RankingPKResponseDTO response = new RankingPKResponseDTO();

        for (int i = 0; i < 10; i++) {
            if (i < topPk.size()) {
                Character c = topPk.get(i);
                String name = (c.getCharName() != null && !c.getCharName().isBlank()) ? c.getCharName() : "empty";
                int kills = c.getPkkills() != null ? c.getPkkills() : 0;
                response.addPlayer(new PlayerRanking(name, kills));
            } else {
                // Si no hay más jugadores en la lista, relleno con vacíos
                response.addPlayer(new PlayerRanking("empty", 0));
            }
        }


        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * @return DTO con el ranking de top 10 PvP
     */
    @Override
    public RankingPvPResponseDTO getTopPvp() {
        // Traigo el top de la DB (puede tener menos de 10)
        List<Character> topPvp = this.characterRepository.findTop10ByPvpkillsOrderByPvpkillsDesc();

        // DTO para la respuesta
        RankingPvPResponseDTO response = new RankingPvPResponseDTO();

        for (int i = 0; i < 10; i++) {
            if (i < topPvp.size()) {
                Character c = topPvp.get(i);
                String name = (c.getCharName() != null && !c.getCharName().isBlank()) ? c.getCharName() : "empty";
                int kills = c.getPvpkills() != null ? c.getPvpkills() : 0;
                response.addPlayer(new PlayerRanking(name, kills));
            } else {
                // Si no hay más jugadores en la lista, relleno con vacíos
                response.addPlayer(new PlayerRanking("empty", 0));
            }
        }


        return response;
    }

    /**
     * {@inheritDoc}
     * 
     * @return DTO con el ranking de top 10 clanes
     */
    @Override
    public RankingClanResponseDTO getTopClans() {
        List<Clan> topClans = this.clanRepository.findTop10ByOrderByReputationScoreDesc();
        List<ClanRanking> clanRankingList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            if (i < topClans.size()) {
                Clan clan = topClans.get(i);
                String name = (clan.getClanName() != null && !clan.getClanName().isBlank()) ? clan.getClanName() : "empty";
                Integer hasCastle = clan.getHasCastle() != null ? clan.getHasCastle() : 0;
                Integer reputation = clan.getReputationScore() != null ? clan.getReputationScore() : 0;
                clanRankingList.add(new ClanRanking(name, hasCastle, reputation));
            } else {
                clanRankingList.add(new ClanRanking("empty", 0, 0));
            }
        }

        return new RankingClanResponseDTO(clanRankingList);
    }
}
