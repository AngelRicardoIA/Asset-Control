package com.assetcontrol.computers.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComputerRepository extends JpaRepository<Computer, Long> {

    boolean existsByAsset(String asset);

    boolean existsByHost(String host);
}