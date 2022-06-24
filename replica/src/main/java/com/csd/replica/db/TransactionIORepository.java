package com.csd.replica.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionIORepository extends JpaRepository<TransactionIOEntity, Long> {
    TransactionIOEntity findTopByOrderByIdDesc();
    List<TransactionIOEntity> findByIdGreaterThan(long id);
    List<TransactionIOEntity> findByOwner(String owner);
    TransactionIOEntity findFirstByOwnerOrderByTimestampAsc(String owner);
}