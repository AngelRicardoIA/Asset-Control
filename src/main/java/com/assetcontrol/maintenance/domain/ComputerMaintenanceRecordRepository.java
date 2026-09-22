package com.assetcontrol.maintenance.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ComputerMaintenanceRecordRepository
        extends JpaRepository<ComputerMaintenanceRecord, Long> {

    List<ComputerMaintenanceRecord> findByComputer_IdOrderByPerformedAtDescIdDesc(
            Long computerId
    );

    Optional<ComputerMaintenanceRecord> findFirstByComputer_IdAndPreventiveTrueOrderByPerformedAtDescIdDesc(Long computerId);

    @Query("""
            SELECT record.computer.id, MAX(record.performedAt)
            FROM ComputerMaintenanceRecord record
            WHERE record.preventive = true
            GROUP BY record.computer.id
            """)
    List<Object[]> findLastPreventiveDates();
}
