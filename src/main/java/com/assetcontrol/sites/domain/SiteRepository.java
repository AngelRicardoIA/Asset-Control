package com.assetcontrol.sites.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByActiveTrueOrderByNameAsc();

    Optional<Site> findByIdAndActiveTrue(Long id);

    Optional<Site> findByNameIgnoreCase(String name);
}