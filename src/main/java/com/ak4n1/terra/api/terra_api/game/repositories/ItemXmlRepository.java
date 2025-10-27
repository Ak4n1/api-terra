package com.ak4n1.terra.api.terra_api.game.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ak4n1.terra.api.terra_api.game.entities.ItemXmlEntity;

import java.util.List;

@Repository
public interface ItemXmlRepository extends JpaRepository<ItemXmlEntity, Integer> {
    // JpaRepository ya provee métodos CRUD básicos
    @Query("SELECT i FROM ItemXmlEntity i WHERE i.id = :id")
    List<ItemXmlEntity> findAllByIdCustom(@Param("id") Integer id);
}