package com.assetcontrol.people.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByExternalIdIgnoreCase(String externalId);
    boolean existsByUsernameIgnoreCase(String username);
    List<Person> findAllByOrderByFullNameAsc();
    Optional<Person> findByUsernameIgnoreCase(String username);
}
