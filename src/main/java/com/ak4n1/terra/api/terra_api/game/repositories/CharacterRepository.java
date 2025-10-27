package com.ak4n1.terra.api.terra_api.game.repositories;

import com.ak4n1.terra.api.terra_api.game.dto.PlayerRanking;
import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ak4n1.terra.api.terra_api.game.entities.Character;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CharacterRepository extends JpaRepository<Character, Integer> {

    @Query("SELECT c FROM Character c WHERE LOWER(c.accountName) = LOWER(:accountName)")
    List<Character> getCharactersByAccountName(@Param("accountName") String accountName);
    
    List<Character> findByAccountName(String accountName);
    
    @Query("SELECT c FROM Character c WHERE c.clanid = :clanId")
    List<Character> findAllByClanid(@Param("clanId") Integer clanId);

    Optional<Character> findByCharId(int charId);

    // Método para obtener personajes por email con paginación
    @Query("SELECT c FROM Character c " +
           "JOIN AccountGame ag ON c.accountName = ag.login " +
           "WHERE ag.email = :email " +
           "ORDER BY c.level DESC, c.charName ASC")
    Page<Character> findCharactersByEmailWithPagination(@Param("email") String email, Pageable pageable);

    // Método para obtener personajes por email sin paginación (para compatibilidad)
    @Query("SELECT c FROM Character c " +
           "JOIN AccountGame ag ON c.accountName = ag.login " +
           "WHERE ag.email = :email " +
           "ORDER BY c.level DESC, c.charName ASC")
    List<Character> findCharactersByEmail(@Param("email") String email);

    // Método para obtener personajes por email con límite específico
    @Query("SELECT c FROM Character c " +
           "JOIN AccountGame ag ON c.accountName = ag.login " +
           "WHERE ag.email = :email " +
           "ORDER BY c.level DESC, c.charName ASC")
    List<Character> findCharactersByEmailWithLimit(@Param("email") String email, Pageable pageable);

    // Método para contar personajes por email
    @Query("SELECT COUNT(c) FROM Character c " +
           "JOIN AccountGame ag ON c.accountName = ag.login " +
           "WHERE ag.email = :email")
    long countCharactersByEmail(@Param("email") String email);

    @Query("SELECT c FROM Character c ORDER BY c.pvpkills DESC")
    List<Character> findTop10ByPvpkillsOrderByPvpkillsDesc( );

    @Query("SELECT c FROM Character c ORDER BY c.pkkills DESC")
    List<Character> findTop10ByPkkillsOrderByPkkillsDesc( );

    @Query("SELECT COUNT(c) FROM Character c WHERE c.online = 1")
    long countOnlineCharacters();

    @Query("SELECT COUNT(c) FROM Character c")
    long countTotalCharacters();

}
