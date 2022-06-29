package com.csd.replica.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {
    ResourceEntity findTopByOrderByIdDesc();
    List<ResourceEntity> findByIdGreaterThan(long id);
    List<ResourceEntity> findByOwner(String owner);

    List<ResourceEntity> findByAsset(String asset);
    ResourceEntity findFirstByOwnerOrderByTimestampAsc(String owner);
}