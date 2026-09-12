package com.assetcontrol.assignments.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}