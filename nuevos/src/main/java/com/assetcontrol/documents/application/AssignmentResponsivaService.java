package com.assetcontrol.documents.application;

import com.assetcontrol.assignments.application.AssignmentNotFoundException;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AssignmentResponsivaService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ComputerAssignmentRepository assignmentRepository;
    private final DocxTemplateRenderer renderer;

    public AssignmentResponsivaService(
            ComputerAssignmentRepository assignmentRepository,
            DocxTemplateRenderer renderer
    ) {
        this.assignmentRepository = assignmentRepository;
        this.renderer = renderer;
    }

    @Transactional(readOnly = true)
    public GeneratedResponsiva generate(Long computerId, Long assignmentId) {
        var assignment = assignmentRepository.findDetailedById(assignmentId)
                .filter(value -> value.getComputer().getId().equals(computerId))
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));

        var person = assignment.getPerson();
        var computer = assignment.getComputer();

        if (assignment.getAssignedAt() == null) {
            throw new ResponsivaGenerationException("La asignación no tiene fecha registrada.");
        }

        Map<String, String> values = new LinkedHashMap<>();
        values.put("employee.fullName", required(person.getFullName(), "nombre completo"));
        values.put("employee.number", required(person.getExternalId(), "número de empleado"));
        values.put("computer.model", required(computer.getModel(), "modelo"));
        values.put("computer.asset", required(computer.getAsset(), "asset"));
        values.put("computer.serialNumber", required(computer.getSerialNumber(), "número de serie"));
        values.put("assignment.assignedAt", assignment.getAssignedAt().format(DATE_FORMAT));

        String host = computer.getHost() == null ? "equipo" : computer.getHost();
        String safeHost = host.replaceAll("[^A-Za-z0-9._-]", "_");
        String filename = "responsiva-" + safeHost + "-asignacion-" + assignment.getId() + ".docx";

        return new GeneratedResponsiva(filename, renderer.render(values));
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ResponsivaGenerationException("Falta registrar el dato: " + label + ".");
        }
        return value;
    }
}
