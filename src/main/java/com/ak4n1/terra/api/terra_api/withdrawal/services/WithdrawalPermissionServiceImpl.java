package com.ak4n1.terra.api.terra_api.withdrawal.services;

import com.ak4n1.terra.api.terra_api.game.entities.Character;
import com.ak4n1.terra.api.terra_api.game.l2j.util.ClassNameUtil;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.withdrawal.dto.CharacterPermissionDTO;
import com.ak4n1.terra.api.terra_api.withdrawal.entities.CharacterWithdrawalPermission;
import com.ak4n1.terra.api.terra_api.withdrawal.entities.WithdrawalPermissionAudit;
import com.ak4n1.terra.api.terra_api.withdrawal.repositories.CharacterWithdrawalPermissionRepository;
import com.ak4n1.terra.api.terra_api.withdrawal.repositories.WithdrawalPermissionAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de permisos de retiro.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Service
public class WithdrawalPermissionServiceImpl implements WithdrawalPermissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawalPermissionServiceImpl.class);

    @Autowired
    private CharacterWithdrawalPermissionRepository permissionRepository;

    @Autowired
    private WithdrawalPermissionAuditRepository auditRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Override
    public List<CharacterPermissionDTO> getCharactersWithPermissionStatus(String email) {
        // Obtener todos los personajes del usuario
        List<Character> characters = characterRepository.findCharactersByEmail(email);
        
        // Obtener IDs de personajes con permiso
        Set<Integer> allowedCharacterIds = permissionRepository
                .findCharacterIdsByAccountEmail(email)
                .stream()
                .collect(Collectors.toSet());

        // Mapear a DTOs con estado de permiso
        return characters.stream()
                .map(character -> new CharacterPermissionDTO(
                        character.getCharId(),
                        character.getCharName(),
                        character.getLevel(),
                        character.getClassid(),
                        ClassNameUtil.getClassName(character.getClassid()),
                        allowedCharacterIds.contains(character.getCharId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean grantPermission(String email, Integer characterId, String characterName, String ipAddress) {
        try {
            // Verificar si ya tiene permiso
            if (permissionRepository.existsByCharacterId(characterId)) {
                LOGGER.warn("Character {} already has withdrawal permission", characterId);
                return true; // Ya tiene permiso, no es error
            }

            // Crear el permiso
            CharacterWithdrawalPermission permission = new CharacterWithdrawalPermission(
                    email, characterId, characterName
            );
            permissionRepository.save(permission);

            // Registrar en auditoría
            WithdrawalPermissionAudit audit = new WithdrawalPermissionAudit(
                    email, characterId, characterName,
                    WithdrawalPermissionAudit.Action.GRANTED,
                    ipAddress
            );
            auditRepository.save(audit);

            LOGGER.info("Granted withdrawal permission to character {} for account {}", 
                    characterName, email);
            return true;

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Race condition: otro request ya insertó el permiso
            LOGGER.info("Permission already exists for character {} (concurrent request)", characterId);
            return true; // El permiso existe, misión cumplida
        } catch (Exception e) {
            LOGGER.error("Error granting permission to character {}: {}", characterId, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean revokePermission(String email, Integer characterId, String characterName, String ipAddress) {
        try {
            // Verificar si tiene permiso
            if (!permissionRepository.existsByCharacterId(characterId)) {
                LOGGER.warn("Character {} does not have withdrawal permission", characterId);
                return true; // No tiene permiso, no es error
            }

            // Eliminar el permiso
            permissionRepository.deleteByAccountEmailAndCharacterId(email, characterId);

            // Registrar en auditoría
            WithdrawalPermissionAudit audit = new WithdrawalPermissionAudit(
                    email, characterId, characterName,
                    WithdrawalPermissionAudit.Action.REVOKED,
                    ipAddress
            );
            auditRepository.save(audit);

            LOGGER.info("Revoked withdrawal permission from character {} for account {}", 
                    characterName, email);
            return true;

        } catch (Exception e) {
            LOGGER.error("Error revoking permission from character {}: {}", characterId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasPermission(Integer characterId) {
        return permissionRepository.existsByCharacterId(characterId);
    }
}

