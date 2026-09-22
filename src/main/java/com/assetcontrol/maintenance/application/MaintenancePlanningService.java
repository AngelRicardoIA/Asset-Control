package com.assetcontrol.maintenance.application;

import com.assetcontrol.computers.application.ComputerNotFoundException;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceScheduleChange;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceScheduleRepository;
import com.assetcontrol.maintenance.domain.MaintenancePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class MaintenancePlanningService {

    private final MaintenancePolicyRepository policies;
    private final ComputerMaintenanceScheduleRepository schedules;
    private final ComputerRepository computers;

    public MaintenancePlanningService(
            MaintenancePolicyRepository policies,
            ComputerMaintenanceScheduleRepository schedules,
            ComputerRepository computers
    ) {
        this.policies = policies;
        this.schedules = schedules;
        this.computers = computers;
    }

    @Transactional
    public void updatePolicy(int intervalMonths, int warningDays) {
        var policy = policies.findById(1L).orElseThrow();
        policy.update(intervalMonths, warningDays);
    }

    @Transactional
    public void schedule(Long computerId, LocalDate date, String reason, String recordedBy) {
        if (date == null || reason == null || reason.isBlank() || recordedBy == null || recordedBy.isBlank()) {
            throw new IllegalArgumentException("Indica fecha, motivo y quién programó la revisión.");
        }
        var computer = computers.findById(computerId)
                .orElseThrow(() -> new ComputerNotFoundException(computerId));
        schedules.save(new ComputerMaintenanceScheduleChange(computer, date, reason.trim(), recordedBy.trim()));
    }
}
