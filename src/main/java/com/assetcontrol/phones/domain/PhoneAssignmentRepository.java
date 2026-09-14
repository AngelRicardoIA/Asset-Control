package com.assetcontrol.phones.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PhoneAssignmentRepository extends JpaRepository<PhoneAssignment, Long> {

    boolean existsByPhoneIdAndPersonIdAndReturnedAtIsNull(Long phoneId, Long personId);

    boolean existsByPhoneIdAndReturnedAtIsNull(Long phoneId);

    @Query("""
            SELECT assignment
            FROM PhoneAssignment assignment
            JOIN FETCH assignment.person
            WHERE assignment.phone.id = :phoneId
            AND assignment.returnedAt IS NULL
            ORDER BY assignment.assignedAt DESC, assignment.id DESC
            """)
    List<PhoneAssignment> findActiveByPhoneIdWithPerson(@Param("phoneId") Long phoneId);

    @Query("""
            SELECT assignment
            FROM PhoneAssignment assignment
            JOIN FETCH assignment.person
            WHERE assignment.phone.id = :phoneId
            ORDER BY assignment.assignedAt DESC, assignment.id DESC
            """)
    List<PhoneAssignment> findHistoryByPhoneIdWithPerson(@Param("phoneId") Long phoneId);

    @Query("""
            SELECT assignment
            FROM PhoneAssignment assignment
            JOIN FETCH assignment.phone
            JOIN FETCH assignment.person
            WHERE assignment.phone.id IN :phoneIds
            AND assignment.returnedAt IS NULL
            ORDER BY assignment.assignedAt DESC, assignment.id DESC
            """)
    List<PhoneAssignment> findActiveByPhoneIdsWithPerson(
            @Param("phoneIds") Collection<Long> phoneIds
    );

    @Query("""
            SELECT assignment
            FROM PhoneAssignment assignment
            JOIN FETCH assignment.phone
            JOIN FETCH assignment.person
            WHERE assignment.id = :assignmentId
            """)
    Optional<PhoneAssignment> findByIdWithPhoneAndPerson(
            @Param("assignmentId") Long assignmentId
    );
}