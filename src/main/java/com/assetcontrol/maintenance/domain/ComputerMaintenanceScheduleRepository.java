package com.assetcontrol.maintenance.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComputerMaintenanceScheduleRepository extends JpaRepository<ComputerMaintenanceScheduleChange, Long> {

    @EntityGraph(attributePaths = "computer")
    List<ComputerMaintenanceScheduleChange> findAllByOrderByIdDesc();

    List<ComputerMaintenanceScheduleChange> findByComputer_IdOrderByIdDesc(Long computerId);
}
