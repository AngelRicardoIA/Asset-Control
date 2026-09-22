package com.assetcontrol.people.domain;

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

    @Query("""
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
            ORDER BY person.fullName, person.username
            """)
    List<Person> search(@Param("query") String query);
}
