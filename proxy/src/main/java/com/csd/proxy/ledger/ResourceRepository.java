package com.csd.proxy.ledger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {
    ResourceEntity findTopByOrderByIdDesc();
    List<ResourceEntity> findByIdGreaterThan(long id);
    List<ResourceEntity> findByOwner(String owner);
    ResourceEntity findFirstByOwnerOrderByTimestampAsc(String owner);
}