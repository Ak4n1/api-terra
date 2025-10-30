package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de estadísticas generales del juego.
 * 
 * <p>Este servicio proporciona acceso a estadísticas agregadas como el número
 * de personajes en línea y el total de personajes creados en el servidor.
 * 
 * @see StatsService
 * @see CharacterRepository
 * @author ak4n1
 * @since 1.0
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private CharacterRepository characterRepository;

    /**
     * {@inheritDoc}
     * 
     * @return Número de personajes online
     */
    @Override
    public long getOnlineCharacterCount() {
        return characterRepository.countOnlineCharacters();
    }

    /**
     * {@inheritDoc}
     * 
     * @return Número total de personajes
     */
    @Override
    public long getTotalCharacterCount() {
        return characterRepository.countTotalCharacters();
    }
}
