package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.CharacterResponseDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;

import java.util.List;

public interface CharacterService {

    List<Character> getCharactersByAccountName(String accountName);

    List<CharacterResponseDTO> getCharactersDTOByAccountName(String accountName);

    List<CharacterResponseDTO> getCharactersByEmail(String email);
}
