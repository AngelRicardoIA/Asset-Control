package com.assetcontrol.phones.maintenance.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhoneMaintenanceRecordRepository
        extends JpaRepository<PhoneMaintenanceRecord, Long> {

    List<PhoneMaintenanceRecord> findByPhone_IdOrderByPerformedAtDescIdDesc(
            Long phoneId
    );
}
