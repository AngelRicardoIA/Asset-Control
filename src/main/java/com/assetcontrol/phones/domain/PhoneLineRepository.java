package com.assetcontrol.phones.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhoneLineRepository extends JpaRepository<PhoneLine, Long> {

    boolean existsByNumberIgnoreCase(String number);

    boolean existsByNumberIgnoreCaseAndIdNot(String number, Long id);

    @Query("""
            SELECT phoneLine
            FROM PhoneLine phoneLine
            WHERE NOT EXISTS (
                SELECT phone.id
                FROM Phone phone
                WHERE phone.phoneLine = phoneLine
            )
            ORDER BY phoneLine.number ASC
            """)
    List<PhoneLine> findAllAvailableForPhone();

    @Query("""
            SELECT phoneLine
            FROM PhoneLine phoneLine
            WHERE NOT EXISTS (
                SELECT phone.id
                FROM Phone phone
                WHERE phone.phoneLine = phoneLine
            )
            OR EXISTS (
                SELECT phone.id
                FROM Phone phone
                WHERE phone.phoneLine = phoneLine
                AND phone.id = :phoneId
            )
            ORDER BY phoneLine.number ASC
            """)
    List<PhoneLine> findAllSelectableForPhone(@Param("phoneId") Long phoneId);
}