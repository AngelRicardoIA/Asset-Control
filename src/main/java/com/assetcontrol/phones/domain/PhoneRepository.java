package com.assetcontrol.phones.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhoneRepository extends JpaRepository<Phone, Long> {

    boolean existsByImeiIgnoreCase(String imei);

    boolean existsByImeiIgnoreCaseAndIdNot(String imei, Long id);

    boolean existsByPhoneLineId(Long phoneLineId);

    boolean existsByPhoneLineIdAndIdNot(Long phoneLineId, Long id);

    @Query("""
            SELECT phone
            FROM Phone phone
            JOIN FETCH phone.site
            LEFT JOIN FETCH phone.phoneLine
            ORDER BY phone.id DESC
            """)
    List<Phone> findAllWithDetails();

    @Query("""
            SELECT phone
            FROM Phone phone
            JOIN FETCH phone.site
            LEFT JOIN FETCH phone.phoneLine
            WHERE phone.id = :phoneId
            """)
    Optional<Phone> findByIdWithDetails(@Param("phoneId") Long phoneId);
}