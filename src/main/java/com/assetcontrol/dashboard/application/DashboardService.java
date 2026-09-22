package com.assetcontrol.dashboard.application;

import com.assetcontrol.assignments.domain.AssignmentType;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.people.domain.PersonRepository;
import com.assetcontrol.phones.domain.PhoneAssignmentRepository;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import com.assetcontrol.phones.domain.PhoneRepository;
import com.assetcontrol.phones.domain.PhoneStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private final ComputerRepository computerRepository;
    private final PhoneRepository phoneRepository;
    private final PersonRepository personRepository;
    private final ComputerAssignmentRepository computerAssignmentRepository;
    private final PhoneAssignmentRepository phoneAssignmentRepository;

    public DashboardService(
            ComputerRepository computerRepository,
            PhoneRepository phoneRepository,
            PersonRepository personRepository,
            ComputerAssignmentRepository computerAssignmentRepository,
            PhoneAssignmentRepository phoneAssignmentRepository
    ) {
        this.computerRepository = computerRepository;
        this.phoneRepository = phoneRepository;
        this.personRepository = personRepository;
        this.computerAssignmentRepository = computerAssignmentRepository;
        this.phoneAssignmentRepository = phoneAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverview overview() {
        LocalDate today = LocalDate.now();
        DashboardAssetStats computers = new DashboardAssetStats(
                computerRepository.count(),
                computerRepository.countByStatus(ComputerStatus.AVAILABLE),
                computerRepository.countByStatus(ComputerStatus.ASSIGNED),
                computerRepository.countByStatus(ComputerStatus.LOANED),
                computerRepository.countByStatus(ComputerStatus.RETIRED)
        );
        DashboardAssetStats phones = new DashboardAssetStats(
                phoneRepository.count(),
                phoneRepository.countByStatus(PhoneStatus.AVAILABLE),
                phoneRepository.countByStatus(PhoneStatus.ASSIGNED),
                phoneRepository.countByStatus(PhoneStatus.LOANED),
                phoneRepository.countByStatus(PhoneStatus.RETIRED)
        );
        long overdueLoans = computerAssignmentRepository
                .countByAssignmentTypeAndReturnedAtIsNullAndDueDateBefore(AssignmentType.LOAN, today)
                + phoneAssignmentRepository.countByTypeAndReturnedAtIsNullAndDueAtBefore(PhoneAssignmentType.LOAN, today);

        var computerLoans = computerAssignmentRepository
                .findTop5ByAssignmentTypeAndReturnedAtIsNullAndDueDateBeforeOrderByDueDateAscIdAsc(
                        AssignmentType.LOAN, today
                )
                .stream()
                .map(assignment -> new DashboardOverdueLoan(
                        true,
                        assignment.getComputer().getId(),
                        assignment.getComputer().getHost(),
                        assignment.getPerson().getUsername(),
                        assignment.getDueDate()
                ));
        var phoneLoans = phoneAssignmentRepository
                .findTop5ByTypeAndReturnedAtIsNullAndDueAtBeforeOrderByDueAtAscIdAsc(PhoneAssignmentType.LOAN, today)
                .stream()
                .map(assignment -> new DashboardOverdueLoan(
                        false,
                        assignment.getPhone().getId(),
                        assignment.getPhone().getImei(),
                        assignment.getPerson().getUsername(),
                        assignment.getDueAt()
                ));

        var oldestLoans = Stream.concat(computerLoans, phoneLoans)
                .sorted(Comparator.comparing(DashboardOverdueLoan::dueDate))
                .limit(5)
                .toList();

        return new DashboardOverview(computers, phones, personRepository.count(), overdueLoans, oldestLoans);
    }
}
