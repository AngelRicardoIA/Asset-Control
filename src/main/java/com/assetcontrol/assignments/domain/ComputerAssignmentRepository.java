package com.assetcontrol.assignments.domain;

import com.assetcontrol.people.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface ComputerAssignmentRepository
        extends JpaRepository<ComputerAssignment, Long> {

    @Query("""
            SELECT assignment
            FROM ComputerAssignment assignment
            JOIN FETCH assignment.person
            WHERE assignment.computer.id = :computerId
            ORDER BY assignment.assignedAt DESC, assignment.id DESC
            """)
    List<ComputerAssignment> findHistoryByComputerId(
            @Param("computerId") Long computerId
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
    @Query("""
        SELECT assignment
        FROM ComputerAssignment assignment
        JOIN FETCH assignment.computer
        JOIN FETCH assignment.person
        WHERE assignment.id = :assignmentId
        """)
    Optional<ComputerAssignment> findDetailedById(
            @Param("assignmentId") Long assignmentId
    );
    @Query("""
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
    @Query("""
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

    @Query("""
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

    @Query("""
        SELECT DISTINCT person
        FROM ComputerAssignment assignment
        JOIN assignment.person person
        WHERE assignment.returnedAt IS NULL
        AND (:siteId IS NULL OR assignment.computer.site.id = :siteId)
        AND (
            :query = ''
            OR LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY person.fullName, person.username
        """)
    List<Person> findPeopleWithActiveAssignments(
            @Param("query") String query,
            @Param("siteId") Long siteId
    );
}
