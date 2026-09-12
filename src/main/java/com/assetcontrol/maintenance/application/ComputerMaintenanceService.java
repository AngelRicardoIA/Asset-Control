package com.assetcontrol.maintenance.application;

import com.assetcontrol.computers.application.ComputerNotFoundException;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceRecord;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputerMaintenanceService {

    private final ComputerMaintenanceRecordRepository maintenanceRepository;
    private final ComputerRepository computerRepository;

    public ComputerMaintenanceService(
            ComputerMaintenanceRecordRepository maintenanceRepository,
            ComputerRepository computerRepository
    ) {
        this.maintenanceRepository = maintenanceRepository;
        this.computerRepository = computerRepository;
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

    private String normalizeRequired(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Este campo es obligatorio.");
        }

        return normalized;
    }
}