package com.assetcontrol.assignments.domain;

import com.assetcontrol.people.domain.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface ComputerAssignmentRepository
        extends JpaRepository<ComputerAssignment, Long> {

    @Query(value = """
            SELECT assignment
            FROM ComputerAssignment assignment
            JOIN FETCH assignment.person
            WHERE assignment.computer.id = :computerId
            ORDER BY assignment.assignedAt DESC, assignment.id DESC
            """)
    List<ComputerAssignment> findHistoryByComputerId(
            @Param("computerId") Long computerId
    );

    @Query(value = """
            SELECT assignment
            FROM ComputerAssignment assignment
            JOIN FETCH assignment.computer computer
            JOIN FETCH computer.site
            JOIN FETCH assignment.person
            WHERE assignment.person.id = :personId
            ORDER BY assignment.assignedAt DESC, assignment.id DESC
            """)
    List<ComputerAssignment> findHistoryByPersonIdWithComputer(
            @Param("personId") Long personId
    );

    boolean existsByComputer_IdAndPerson_IdAndReturnedAtIsNull(
            Long computerId,
            Long personId
    );

    boolean existsByComputer_IdAndReturnedAtIsNull(Long computerId);

    boolean existsByComputer_IdAndAssignmentTypeAndReturnedAtIsNull(
            Long computerId,
            AssignmentType assignmentType
    );
    @Query(value = """
        SELECT assignment
        FROM ComputerAssignment assignment
        JOIN FETCH assignment.computer
        JOIN FETCH assignment.person
        WHERE assignment.id = :assignmentId
        """)
    Optional<ComputerAssignment> findDetailedById(
            @Param("assignmentId") Long assignmentId
    );
    @Query(value = """
        SELECT assignment
        FROM ComputerAssignment assignment
        JOIN FETCH assignment.computer
        JOIN FETCH assignment.person
        WHERE assignment.computer.id IN :computerIds
        AND assignment.returnedAt IS NULL
        ORDER BY assignment.person.username
        """)
    List<ComputerAssignment> findActiveByComputerIdsWithPerson(
            @Param("computerIds") Collection<Long> computerIds
    );
    @Query(value = """
        SELECT assignment
        FROM ComputerAssignment assignment
        JOIN FETCH assignment.computer
        JOIN FETCH assignment.person
        WHERE assignment.computer.id IN :computerIds
        AND assignment.returnedAt IS NOT NULL
        ORDER BY assignment.computer.id, assignment.returnedAt DESC, assignment.id DESC
        """)
    List<ComputerAssignment> findClosedByComputerIdsWithPerson(
            @Param("computerIds") Collection<Long> computerIds
    );

    @Query(value = """
        SELECT assignment
        FROM ComputerAssignment assignment
        JOIN FETCH assignment.computer computer
        JOIN FETCH computer.site
        JOIN FETCH assignment.person
        WHERE assignment.person.id IN :personIds
        AND assignment.returnedAt IS NULL
        ORDER BY assignment.person.fullName, computer.host
        """)
    List<ComputerAssignment> findActiveByPersonIdsWithComputer(
            @Param("personIds") Collection<Long> personIds
    );

    @Query(value = """
        SELECT person
        FROM Person person
        WHERE EXISTS (
            SELECT assignment.id
            FROM ComputerAssignment assignment
            WHERE assignment.person = person
            AND assignment.returnedAt IS NULL
            AND (:siteId IS NULL OR assignment.computer.site.id = :siteId)
        )
        AND (
            :query = ''
            OR LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY
            CASE WHEN :chronological = false AND :descending = false THEN LOWER(person.fullName) END ASC,
            CASE WHEN :chronological = false AND :descending = true THEN LOWER(person.fullName) END DESC,
            CASE WHEN :chronological = false AND :descending = false THEN LOWER(person.username) END ASC,
            CASE WHEN :chronological = false AND :descending = true THEN LOWER(person.username) END DESC,
            CASE WHEN :chronological = true AND :descending = false THEN person.createdAt END ASC,
            CASE WHEN :chronological = true AND :descending = true THEN person.createdAt END DESC,
            CASE WHEN :descending = false THEN person.id END ASC,
            CASE WHEN :descending = true THEN person.id END DESC
        """, countQuery = """
        SELECT COUNT(person)
        FROM Person person
        WHERE EXISTS (
            SELECT assignment.id
            FROM ComputerAssignment assignment
            WHERE assignment.person = person
            AND assignment.returnedAt IS NULL
            AND (:siteId IS NULL OR assignment.computer.site.id = :siteId)
        )
        AND (
            :query = ''
            OR LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        """)
    Page<Person> findPeopleWithActiveAssignmentsPage(
            @Param("query") String query,
            @Param("siteId") Long siteId,
            @Param("chronological") boolean chronological,
            @Param("descending") boolean descending,
            Pageable pageable
    );
}
