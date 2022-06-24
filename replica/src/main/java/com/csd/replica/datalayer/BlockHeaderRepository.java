package com.csd.replica.datalayer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockHeaderRepository extends JpaRepository<BlockHeaderEntity, Long> {

}