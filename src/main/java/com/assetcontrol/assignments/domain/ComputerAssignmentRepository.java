package com.assetcontrol.assignments.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComputerAssignmentRepository
        extends JpaRepository<ComputerAssignment, Long> {

    List<ComputerAssignment> findByComputer_IdOrderByAssignedAtDesc(Long computerId);

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