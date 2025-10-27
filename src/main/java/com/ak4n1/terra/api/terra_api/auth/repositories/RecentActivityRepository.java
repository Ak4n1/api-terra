package com.ak4n1.terra.api.terra_api.auth.repositories;


import com.ak4n1.terra.api.terra_api.auth.entities.RecentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentActivityRepository extends JpaRepository<RecentActivity, Long> {

    List<RecentActivity> findByAccountMaster_EmailOrderByTimestampDesc(String email);

}