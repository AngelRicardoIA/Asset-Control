package com.assetcontrol.phones.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneLineRepository extends JpaRepository<PhoneLine, Long> {

    boolean existsByNumberIgnoreCase(String number);

    boolean existsByNumberIgnoreCaseAndIdNot(String number, Long id);
}