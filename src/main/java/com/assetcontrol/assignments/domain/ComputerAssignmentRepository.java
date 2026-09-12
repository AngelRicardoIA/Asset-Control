package com.assetcontrol.assignments.domain;

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
}