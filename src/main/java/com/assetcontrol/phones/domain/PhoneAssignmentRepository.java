package com.assetcontrol.phones.domain;

import com.assetcontrol.people.domain.Person;
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
            WHERE assignment.phone.id IN :phoneIds
            AND assignment.returnedAt IS NOT NULL
            ORDER BY assignment.phone.id, assignment.returnedAt DESC, assignment.id DESC
            """)
    List<PhoneAssignment> findClosedByPhoneIdsWithPerson(
            @Param("phoneIds") Collection<Long> phoneIds
    );

    @Query("""
            SELECT assignment
            FROM PhoneAssignment assignment
            JOIN FETCH assignment.phone phone
            JOIN FETCH phone.site
            LEFT JOIN FETCH phone.phoneLine
            JOIN FETCH assignment.person
            WHERE assignment.person.id IN :personIds
            AND assignment.returnedAt IS NULL
            ORDER BY assignment.person.fullName, phone.imei
            """)
    List<PhoneAssignment> findActiveByPersonIdsWithPhone(
            @Param("personIds") Collection<Long> personIds
    );

    @Query("""
            SELECT DISTINCT person
            FROM PhoneAssignment assignment
            JOIN assignment.person person
            WHERE assignment.returnedAt IS NULL
            AND (:siteId IS NULL OR assignment.phone.site.id = :siteId)
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
