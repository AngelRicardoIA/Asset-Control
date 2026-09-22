package com.assetcontrol.people.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByExternalIdIgnoreCase(String externalId);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByExternalIdIgnoreCaseAndIdNot(String externalId, Long id);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    List<Person> findAllByOrderByFullNameAsc();
    Optional<Person> findByExternalIdIgnoreCase(String externalId);
    Optional<Person> findByUsernameIgnoreCase(String username);

    @Query(value = """
            SELECT person
            FROM Person person
            WHERE :query = ''
            OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(person.jobTitle, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(person.department, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(person.managerName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
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
            WHERE :query = ''
            OR LOWER(person.externalId) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(person.email) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(person.jobTitle, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(person.department, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(COALESCE(person.managerName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Person> searchPage(
            @Param("query") String query,
            @Param("chronological") boolean chronological,
            @Param("descending") boolean descending,
            Pageable pageable
    );
}
