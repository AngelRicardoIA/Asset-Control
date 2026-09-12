package com.assetcontrol.computers.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComputerRepository extends JpaRepository<Computer, Long> {

    boolean existsByAssetIgnoreCase(String asset);

    boolean existsByHostIgnoreCase(String host);

    boolean existsByAssetIgnoreCaseAndIdNot(String asset, Long id);

    boolean existsByHostIgnoreCaseAndIdNot(String host, Long id);

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
        ORDER BY computer.host
        """)
    List<Computer> search(
            @Param("query") String query,
            @Param("status") ComputerStatus status,
            @Param("type") ComputerType type
    );

    @Query("""
        SELECT computer
        FROM Computer computer
        JOIN FETCH computer.site
        WHERE computer.id = :id
        """)
    Optional<Computer> findDetailedById(@Param("id") Long id);
}