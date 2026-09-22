package com.assetcontrol.phones.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            SELECT phone
            FROM Phone phone
            JOIN FETCH phone.site
            LEFT JOIN FETCH phone.phoneLine
            ORDER BY phone.id DESC
            """)
    List<Phone> findAllWithDetails();

    @Query(value = """
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
            ORDER BY
                CASE WHEN :chronological = false AND :descending = false THEN LOWER(COALESCE(phoneLine.number, phone.imei)) END ASC,
                CASE WHEN :chronological = false AND :descending = true THEN LOWER(COALESCE(phoneLine.number, phone.imei)) END DESC,
                CASE WHEN :chronological = false AND :descending = false THEN LOWER(phone.imei) END ASC,
                CASE WHEN :chronological = false AND :descending = true THEN LOWER(phone.imei) END DESC,
                CASE WHEN :chronological = true AND :descending = false THEN phone.createdAt END ASC,
                CASE WHEN :chronological = true AND :descending = true THEN phone.createdAt END DESC,
                CASE WHEN :descending = false THEN phone.id END ASC,
                CASE WHEN :descending = true THEN phone.id END DESC
            """, countQuery = """
            SELECT COUNT(phone)
            FROM Phone phone
            LEFT JOIN phone.phoneLine phoneLine
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
            """)
    Page<Phone> searchPage(
            @Param("query") String query,
            @Param("status") PhoneStatus status,
            @Param("siteId") Long siteId,
            @Param("chronological") boolean chronological,
            @Param("descending") boolean descending,
            Pageable pageable
    );

    @Query(value = """
            SELECT phone
            FROM Phone phone
            JOIN FETCH phone.site
            LEFT JOIN FETCH phone.phoneLine
            WHERE phone.id = :phoneId
            """)
    Optional<Phone> findByIdWithDetails(@Param("phoneId") Long phoneId);
}
