package com.assetcontrol.phones.maintenance.application;

import com.assetcontrol.phones.maintenance.domain.PhoneMaintenanceType;

import java.time.LocalDate;

public record CreatePhoneMaintenanceRecordCommand(
        Long phoneId,
        PhoneMaintenanceType recordType,
        LocalDate performedAt,
        String description,
        String performedBy
) {
}
