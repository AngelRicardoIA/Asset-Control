package com.assetcontrol.phones.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long> {

    boolean existsByImeiIgnoreCase(String imei);

    boolean existsByImeiIgnoreCaseAndIdNot(String imei, Long id);
}