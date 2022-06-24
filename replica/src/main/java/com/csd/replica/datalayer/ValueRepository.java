package com.csd.replica.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValueRepository extends JpaRepository<ValueEntity, Long> {
    ValueEntity findTopByOrderByIdDesc();
    List<ValueEntity> findByIdGreaterThan(long id);
    List<ValueEntity> findByOwner(String owner);
    ValueEntity findFirstByOwnerOrderByTimestampAsc(String owner);
}