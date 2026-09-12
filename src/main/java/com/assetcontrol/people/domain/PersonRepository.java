package com.assetcontrol.people.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByExternalIdIgnoreCase(String externalId);
    boolean existsByUsernameIgnoreCase(String username);
    List<Person> findAllByOrderByFullNameAsc();
}