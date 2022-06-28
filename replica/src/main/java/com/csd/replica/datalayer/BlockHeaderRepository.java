package com.csd.replica.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockHeaderRepository extends JpaRepository<BlockHeaderEntity, Long> {
    BlockHeaderEntity findTopByOrderByIdDesc();

    List<ResourceEntity> findById(String id);
}