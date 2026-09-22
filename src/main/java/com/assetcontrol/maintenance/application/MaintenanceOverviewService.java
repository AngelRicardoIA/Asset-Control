package com.assetcontrol.maintenance.application;

import com.assetcontrol.computers.application.ComputerNotFoundException;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceRecordRepository;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceScheduleRepository;
import com.assetcontrol.maintenance.domain.MaintenancePolicy;
import com.assetcontrol.maintenance.domain.MaintenancePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import com.assetcontrol.maintenance.domain.ComputerMaintenanceScheduleChange;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class MaintenanceOverviewService {

    private final ComputerRepository computers;
    private final ComputerMaintenanceRecordRepository records;
    private final ComputerMaintenanceScheduleRepository schedules;
    private final MaintenancePolicyRepository policies;

    public MaintenanceOverviewService(
            ComputerRepository computers,
            ComputerMaintenanceRecordRepository records,
            ComputerMaintenanceScheduleRepository schedules,
            MaintenancePolicyRepository policies
    ) {
        this.computers = computers;
        this.records = records;
        this.schedules = schedules;
        this.policies = policies;
    }

    public MaintenancePolicy policy() {
        return policies.findById(1L).orElseThrow(() -> new IllegalStateException("Falta configurar mantenimientos."));
    }

    public MaintenanceOverview overview() {
        MaintenancePolicy policy = policy();
        Map<Long, LocalDate> lastDone = new HashMap<>();
        for (Object[] row : records.findLastPreventiveDates()) {
            lastDone.put((Long) row[0], (LocalDate) row[1]);
        }
        Map<Long, LocalDate> nextDates = new HashMap<>();
        schedules.findAllByOrderByIdDesc().forEach(change ->
                nextDates.putIfAbsent(change.getComputer().getId(), change.getNextDueAt())
        );

        LocalDate today = LocalDate.now();
        var items = computers.findByStatusNot(ComputerStatus.RETIRED).stream()
                .map(computer -> {
                    LocalDate last = lastDone.get(computer.getId());
                    LocalDate firstDate = last == null
                            ? computer.getCreatedAt().toLocalDate()
                            : last;
                    LocalDate due = nextDates.getOrDefault(
                            computer.getId(), firstDate.plusMonths(policy.getIntervalMonths())
                    );
                    MaintenanceDueStatus status = due.isBefore(today)
                            ? MaintenanceDueStatus.OVERDUE
                            : !due.isAfter(today.plusDays(policy.getWarningDays()))
                            ? MaintenanceDueStatus.UPCOMING
                            : MaintenanceDueStatus.CURRENT;
                    return new ComputerMaintenanceDue(computer, last, due, status);
                })
                .sorted(Comparator.comparing(ComputerMaintenanceDue::nextDueAt)
                        .thenComparing(item -> item.computer().getHost(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new MaintenanceOverview(
                items,
                items.stream().filter(item -> item.status() == MaintenanceDueStatus.OVERDUE).count(),
                items.stream().filter(item -> item.status() == MaintenanceDueStatus.UPCOMING).count(),
                items.stream().filter(item -> item.status() == MaintenanceDueStatus.CURRENT).count()
        );
    }

    public List<ComputerMaintenanceScheduleChange> scheduleHistory(Long computerId) {
        return schedules.findByComputer_IdOrderByIdDesc(computerId);
    }

    public ComputerMaintenanceDue findByComputerId(Long computerId) {
        return overview().computers().stream()
                .filter(item -> item.computer().getId().equals(computerId))
                .findFirst()
                .orElseThrow(() -> new ComputerNotFoundException(computerId));
    }
}
