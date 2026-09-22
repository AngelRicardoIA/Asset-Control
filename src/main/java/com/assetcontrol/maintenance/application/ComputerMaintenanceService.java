package com.assetcontrol.maintenance.application;

import com.assetcontrol.computers.application.ComputerNotFoundException;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceRecord;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceRecordRepository;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceScheduleChange;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceScheduleRepository;
import com.assetcontrol.maintenance.domain.MaintenanceCheck;
import com.assetcontrol.maintenance.domain.MaintenancePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputerMaintenanceService {

    private final ComputerMaintenanceRecordRepository maintenanceRepository;
    private final ComputerRepository computerRepository;
    private final ComputerMaintenanceScheduleRepository schedules;
    private final MaintenancePolicyRepository policies;

    public ComputerMaintenanceService(
            ComputerMaintenanceRecordRepository maintenanceRepository,
            ComputerRepository computerRepository,
            ComputerMaintenanceScheduleRepository schedules,
            MaintenancePolicyRepository policies
    ) {
        this.maintenanceRepository = maintenanceRepository;
        this.computerRepository = computerRepository;
        this.schedules = schedules;
        this.policies = policies;
    }

    public List<ComputerMaintenanceRecord> findByComputerId(Long computerId) {
        return maintenanceRepository.findByComputer_IdOrderByPerformedAtDescIdDesc(
                computerId
        );
    }

    @Transactional
    public ComputerMaintenanceRecord create(
            CreateComputerMaintenanceRecordCommand command
    ) {
        Computer computer = computerRepository.findById(command.computerId())
                .orElseThrow(() -> new ComputerNotFoundException(command.computerId()));

        ComputerMaintenanceRecord record = new ComputerMaintenanceRecord(
                computer,
                command.recordType(),
                command.performedAt() == null
                        ? LocalDate.now()
                        : command.performedAt(),
                normalizeRequired(command.description()),
                normalizeRequired(command.performedBy())
        );

        return maintenanceRepository.save(record);
    }

    @Transactional
    public ComputerMaintenanceRecord createPreventive(CreatePreventiveMaintenanceCommand command) {
        Computer computer = computerRepository.findById(command.computerId())
                .orElseThrow(() -> new ComputerNotFoundException(command.computerId()));
        LocalDate performedAt = command.performedAt();
        if (performedAt == null || performedAt.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de revisión no puede ser futura.");
        }
        if (command.updatesCheck() == null || command.driversCheck() == null
                || command.externalCleaning() == null || command.internalCleaning() == null) {
            throw new IllegalArgumentException("Completa todas las comprobaciones.");
        }
        boolean skipped = command.updatesCheck() == MaintenanceCheck.NOT_APPLICABLE
                || command.driversCheck() == MaintenanceCheck.NOT_APPLICABLE
                || command.externalCleaning() == MaintenanceCheck.NOT_APPLICABLE
                || command.internalCleaning() == MaintenanceCheck.NOT_APPLICABLE;
        if (skipped && (command.checklistNotes() == null || command.checklistNotes().isBlank())) {
            throw new IllegalArgumentException("Explica las comprobaciones que no aplican.");
        }

        boolean latest = maintenanceRepository
                .findFirstByComputer_IdAndPreventiveTrueOrderByPerformedAtDescIdDesc(command.computerId())
                .map(previous -> !previous.getPerformedAt().isAfter(performedAt))
                .orElse(true);
        String person = normalizeRequired(command.performedBy());
        var record = ComputerMaintenanceRecord.preventive(
                computer, performedAt, normalizeRequired(command.description()), person,
                command.updatesCheck(), command.driversCheck(), command.externalCleaning(),
                command.internalCleaning(), command.checklistNotes() == null ? null : command.checklistNotes().trim()
        );
        maintenanceRepository.save(record);
        if (latest) {
            var policy = policies.findById(1L).orElseThrow();
            schedules.save(new ComputerMaintenanceScheduleChange(
                    computer, performedAt.plusMonths(policy.getIntervalMonths()), "Revisión preventiva completada", person
            ));
        }
        return record;
    }

    private String normalizeRequired(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Este campo es obligatorio.");
        }

        return normalized;
    }
}
