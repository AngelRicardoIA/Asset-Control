package com.assetcontrol.maintenance.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComputerMaintenanceRecordRepository
        extends JpaRepository<ComputerMaintenanceRecord, Long> {

    List<ComputerMaintenanceRecord> findByComputer_IdOrderByPerformedAtDescIdDesc(
            Long computerId
    );
}