package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.ClanMemberDTO;
import com.ak4n1.terra.api.terra_api.game.dto.ClanResponseDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;
import com.ak4n1.terra.api.terra_api.game.entities.Clan;
import com.ak4n1.terra.api.terra_api.game.entities.ClanWar;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ClanRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ClanWarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de clanes.
 * 
 * <p>Este servicio proporciona métodos para consultar información detallada de clanes,
 * incluyendo miembros con estado online, guerras activas, alianzas y datos del líder.
 * 
 * @see ClanService
 * @see ClanRepository
 * @see CharacterRepository
 * @author ak4n1
 * @since 1.0
 */
@Service
public class ClanServiceImpl implements ClanService {

    private static final Logger logger = LoggerFactory.getLogger(ClanServiceImpl.class);

    @Autowired
    private ClanRepository clanRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private ClanWarRepository clanWarRepository;

    /**
     * {@inheritDoc}
     * 
     * @param clanId ID del clan a consultar
     * @return DTO con toda la información del clan
     * @throws RuntimeException si el clan no existe
     */
    @Override
    public ClanResponseDTO getClanById(Integer clanId) {
        Optional<Clan> optionalClan = clanRepository.findById(clanId);

        if (optionalClan.isEmpty()) {
            throw new RuntimeException("Clan con ID " + clanId + " no encontrado.");
        }

        Clan clan = optionalClan.get();

        // Obtener miembros del clan por clanId
        List<com.ak4n1.terra.api.terra_api.game.entities.Character> characters = characterRepository.findAllByClanid(clanId);

        // Mapear miembros a DTO con nombre y estado online
        List<ClanMemberDTO> memberDTOs = characters.stream()
                .map(c -> new ClanMemberDTO(c.getCharName(), c.getOnline() == 1))
                .collect(Collectors.toList());

        // Obtener guerras donde este clan es clan1
        List<ClanWar> clanWars = clanWarRepository.findWarsWhereClanIsClan1OrClan2(clan.getClanId());

        // Depuración: imprimir cantidad de guerras encontradas
        logger.debug("⚔️ [CLAN] Se encontraron {} guerras donde clan1 es clanId={}", clanWars.size(), clan.getClanId());


        // Verificar si hay alguna guerra activa (state == 1)
        boolean inWar = clanWars.size()!=0;

        logger.debug("⚔️ [CLAN] ¿El clan está en guerra activa? {}", inWar);

        // Construir DTO del clan
        ClanResponseDTO dto = new ClanResponseDTO();
        dto.setClanId(clan.getClanId());
        dto.setClanName(clan.getClanName());
        dto.setClanLevel(clan.getClanLevel());
        dto.setReputationScore(clan.getReputationScore());
        dto.setHasCastle(clan.getHasCastle());
        dto.setBloodAllianceCount(clan.getBloodAllianceCount());
        dto.setBloodOathCount(clan.getBloodOathCount());
        dto.setAllyId(clan.getAllyId());
        dto.setAllyName(clan.getAllyName());
        dto.setLeaderId(clan.getLeaderId());
        dto.setCrestId(clan.getCrestId());
        dto.setCrestLargeId(clan.getCrestLargeId());
        dto.setAllyCrestId(clan.getAllyCrestId());
        dto.setAuctionBidAt(clan.getAuctionBidAt());
        dto.setAllyPenaltyExpiryTime(clan.getAllyPenaltyExpiryTime());
        dto.setAllyPenaltyType(clan.getAllyPenaltyType());
        dto.setCharPenaltyExpiryTime(clan.getCharPenaltyExpiryTime());
        dto.setDissolvingExpiryTime(clan.getDissolvingExpiryTime());
        dto.setNewLeaderId(clan.getNewLeaderId());

        // Añadir miembros y total
        dto.setMembers(memberDTOs);
        dto.setTotalMembers(memberDTOs.size());

        // Setear estado de guerra
        dto.setHasWar(inWar);

        // Obtener nombre del líder si existe
        Optional<com.ak4n1.terra.api.terra_api.game.entities.Character> leader = characterRepository.findById(clan.getLeaderId());
        leader.ifPresent(character -> dto.setLeaderName(character.getCharName()));

        return dto;
    }


}
