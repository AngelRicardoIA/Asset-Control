package com.assetcontrol.phones.maintenance.application;

import com.assetcontrol.phones.application.PhoneNotFoundException;
import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneRepository;
import com.assetcontrol.phones.maintenance.domain.PhoneMaintenanceRecord;
import com.assetcontrol.phones.maintenance.domain.PhoneMaintenanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PhoneMaintenanceService {

    private final PhoneMaintenanceRecordRepository maintenanceRepository;
    private final PhoneRepository phoneRepository;

    public PhoneMaintenanceService(
            PhoneMaintenanceRecordRepository maintenanceRepository,
            PhoneRepository phoneRepository
    ) {
        this.maintenanceRepository = maintenanceRepository;
        this.phoneRepository = phoneRepository;
    }

    public List<PhoneMaintenanceRecord> findByPhoneId(Long phoneId) {
        return maintenanceRepository.findByPhone_IdOrderByPerformedAtDescIdDesc(phoneId);
    }

    @Transactional
    public PhoneMaintenanceRecord create(
            CreatePhoneMaintenanceRecordCommand command
    ) {
        Phone phone = phoneRepository.findById(command.phoneId())
                .orElseThrow(() -> new PhoneNotFoundException(command.phoneId()));

        PhoneMaintenanceRecord record = new PhoneMaintenanceRecord(
                phone,
                command.recordType(),
                command.performedAt() == null
                        ? LocalDate.now()
                        : command.performedAt(),
                normalizeRequired(command.description()),
                normalizeRequired(command.performedBy())
        );

        return maintenanceRepository.save(record);
    }

    private String normalizeRequired(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Este campo es obligatorio.");
        }

        return normalized;
    }
}
