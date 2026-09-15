package com.assetcontrol.labels.application;

import com.assetcontrol.assignments.application.AssignmentNotFoundException;
import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentLabelService {

    private final ComputerAssignmentRepository assignmentRepository;
    private final LabelPrinter printer;
    private final LabelUsernameFormatter usernameFormatter;
    private final String defaultPrinterIp;

    public AssignmentLabelService(
            ComputerAssignmentRepository assignmentRepository,
            LabelPrinter printer,
            LabelUsernameFormatter usernameFormatter,
            @Value("${asset-control.labels.printer-ip:}") String defaultPrinterIp
    ) {
        this.assignmentRepository = assignmentRepository;
        this.printer = printer;
        this.usernameFormatter = usernameFormatter;
        this.defaultPrinterIp = defaultPrinterIp;
    }

    @Transactional(readOnly = true)
    public PreparedLabel prepare(Long computerId, Long assignmentId) {
        var assignment = findActiveAssignment(computerId, assignmentId);
        var computer = assignment.getComputer();
        return new PreparedLabel(defaultPrinterIp, new LabelContent(
                usernameFormatter.format(assignment.getPerson().getUsername()),
                computer.getAsset(),
                computer.getModel(),
                computer.getSerialNumber(),
                computer.getHost()
        ));
    }

    public void print(Long computerId, Long assignmentId, String printerIp, LabelContent content) {
        findActiveAssignment(computerId, assignmentId);
        printer.print(printerIp, content);
    }

    private ComputerAssignment findActiveAssignment(Long computerId, Long assignmentId) {
        return assignmentRepository.findDetailedById(assignmentId)
                .filter(assignment -> assignment.getComputer().getId().equals(computerId))
                .filter(ComputerAssignment::isActive)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
    }

    public record PreparedLabel(String printerIp, LabelContent content) {
    }
}
