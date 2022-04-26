package com.csd.replica.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    TransactionEntity findTopByOrderByIdDesc();
    List<TransactionEntity> findByIdGreaterThan(long id);
    List<TransactionEntity> findByOwner(String owner);
}