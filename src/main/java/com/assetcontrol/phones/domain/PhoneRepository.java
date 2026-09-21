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

    Optional<Phone> findByImeiIgnoreCase(String imei);

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
            LEFT JOIN FETCH phone.phoneLine phoneLine
            WHERE (
                :query = ''
                OR LOWER(phone.imei) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(phone.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(phone.model) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(phone.site.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(phoneLine.number, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR EXISTS (
                    SELECT assignment
                    FROM PhoneAssignment assignment
                    JOIN assignment.person person
                    WHERE assignment.phone = phone
                    AND assignment.returnedAt IS NULL
                    AND (
                        LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
                        OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
                    )
                )
            )
            AND (:status IS NULL OR phone.status = :status)
            AND (:siteId IS NULL OR phone.site.id = :siteId)
            ORDER BY phone.imei
            """)
    List<Phone> search(
            @Param("query") String query,
            @Param("status") PhoneStatus status,
            @Param("siteId") Long siteId
    );

    @Query("""
            SELECT phone
            FROM Phone phone
            JOIN FETCH phone.site
            LEFT JOIN FETCH phone.phoneLine
            WHERE phone.id = :phoneId
            """)
    Optional<Phone> findByIdWithDetails(@Param("phoneId") Long phoneId);
}
