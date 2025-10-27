package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private CharacterRepository characterRepository;

    @Override
    public long getOnlineCharacterCount() {
        return characterRepository.countOnlineCharacters();
    }

    @Override
    public long getTotalCharacterCount() {
        return characterRepository.countTotalCharacters();
    }
}
