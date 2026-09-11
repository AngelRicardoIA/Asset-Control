package com.assetcontrol.sites.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByActiveTrueOrderByNameAsc();

    boolean existsByName(String name);
}