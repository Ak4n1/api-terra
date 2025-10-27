package com.ak4n1.terra.api.terra_api.game.repositories;


import com.ak4n1.terra.api.terra_api.game.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByOwnerId(Integer ownerId);  // Importante: esto lo usás para traer los items del pj
}
