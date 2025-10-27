package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.RankingClanResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPKResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPvPResponseDTO;

import java.util.List;

public interface RankingsService {

     RankingPKResponseDTO getTopPk() ;
     RankingPvPResponseDTO getTopPvp() ;

    RankingClanResponseDTO getTopClans();



}
