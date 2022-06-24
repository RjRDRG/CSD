package com.csd.replica.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockHeaderRepository extends JpaRepository<BlockHeaderEntity, Long> {

}