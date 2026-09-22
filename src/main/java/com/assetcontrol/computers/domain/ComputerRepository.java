package com.assetcontrol.computers.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ComputerRepository extends JpaRepository<Computer, Long> {

    boolean existsByAssetIgnoreCase(String asset);

    boolean existsByHostIgnoreCase(String host);

    boolean existsByAssetIgnoreCaseAndIdNot(String asset, Long id);

    boolean existsByHostIgnoreCaseAndIdNot(String host, Long id);

    Optional<Computer> findByHostIgnoreCase(String host);

    Optional<Computer> findByAssetIgnoreCase(String asset);

    @Query("""
        SELECT computer
        FROM Computer computer
        JOIN FETCH computer.site
        WHERE (
            :query = ''
            OR LOWER(computer.host) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.asset) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.brand) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.model) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.serialNumber) LIKE LOWER(CONCAT('%', :query, '%'))
            OR EXISTS (
                SELECT assignment
                FROM ComputerAssignment assignment
                JOIN assignment.person person
                WHERE assignment.computer = computer
                AND assignment.returnedAt IS NULL
                AND (
                    LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            )
        )
        AND (:status IS NULL OR computer.status = :status)
        AND (:type IS NULL OR computer.type = :type)
        AND (:siteId IS NULL OR computer.site.id = :siteId)
        ORDER BY
            CASE WHEN :chronological = false AND :descending = false THEN LOWER(computer.host) END ASC,
            CASE WHEN :chronological = false AND :descending = true THEN LOWER(computer.host) END DESC,
            CASE WHEN :chronological = true AND :descending = false THEN computer.createdAt END ASC,
            CASE WHEN :chronological = true AND :descending = true THEN computer.createdAt END DESC,
            CASE WHEN :descending = false THEN computer.id END ASC,
            CASE WHEN :descending = true THEN computer.id END DESC
        """, countQuery = """
        SELECT COUNT(computer)
        FROM Computer computer
        WHERE (
            :query = ''
            OR LOWER(computer.host) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.asset) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.brand) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.model) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(computer.serialNumber) LIKE LOWER(CONCAT('%', :query, '%'))
            OR EXISTS (
                SELECT assignment
                FROM ComputerAssignment assignment
                JOIN assignment.person person
                WHERE assignment.computer = computer
                AND assignment.returnedAt IS NULL
                AND (
                    LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            )
        )
        AND (:status IS NULL OR computer.status = :status)
        AND (:type IS NULL OR computer.type = :type)
        AND (:siteId IS NULL OR computer.site.id = :siteId)
        """)
    Page<Computer> searchPage(
            @Param("query") String query,
            @Param("status") ComputerStatus status,
            @Param("type") ComputerType type,
            @Param("siteId") Long siteId,
            @Param("chronological") boolean chronological,
            @Param("descending") boolean descending,
            Pageable pageable
    );

    @Query("""
        SELECT computer
        FROM Computer computer
        JOIN FETCH computer.site
        WHERE computer.id = :id
        """)
    Optional<Computer> findDetailedById(@Param("id") Long id);
}
