package com.assetcontrol.imports.application;

import com.assetcontrol.assignments.application.ComputerAssignmentService;
import com.assetcontrol.assignments.domain.AssignmentType;
import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.people.domain.PersonRepository;
import com.assetcontrol.phones.application.PhoneAssignmentService;
import com.assetcontrol.phones.domain.Phone;
import com.assetcontrol.phones.domain.PhoneAssignment;
import com.assetcontrol.phones.domain.PhoneAssignmentRepository;
import com.assetcontrol.phones.domain.PhoneAssignmentType;
import com.assetcontrol.phones.domain.PhoneRepository;
import com.assetcontrol.phones.domain.PhoneStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AssignmentCsvImportService {

    private static final List<String> COMPUTER_HEADERS = List.of(
            "host", "username", "assignment_type", "assigned_at", "due_date", "returned_at", "notes"
    );
    private static final List<String> PHONE_HEADERS = List.of(
            "imei", "username", "assignment_type", "assigned_at", "due_date", "returned_at", "notes"
    );

    private final ComputerRepository computerRepository;
    private final PersonRepository personRepository;
    private final ComputerAssignmentRepository computerAssignmentRepository;
    private final ComputerAssignmentService computerAssignmentService;
    private final PhoneRepository phoneRepository;
    private final PhoneAssignmentRepository phoneAssignmentRepository;
    private final PhoneAssignmentService phoneAssignmentService;

    public AssignmentCsvImportService(
            ComputerRepository computerRepository,
            PersonRepository personRepository,
            ComputerAssignmentRepository computerAssignmentRepository,
            ComputerAssignmentService computerAssignmentService,
            PhoneRepository phoneRepository,
            PhoneAssignmentRepository phoneAssignmentRepository,
            PhoneAssignmentService phoneAssignmentService
    ) {
        this.computerRepository = computerRepository;
        this.personRepository = personRepository;
        this.computerAssignmentRepository = computerAssignmentRepository;
        this.computerAssignmentService = computerAssignmentService;
        this.phoneRepository = phoneRepository;
        this.phoneAssignmentRepository = phoneAssignmentRepository;
        this.phoneAssignmentService = phoneAssignmentService;
    }

    public AssignmentImportPreview previewComputers(MultipartFile file) {
        return preview(file, AssignmentImportPreview.Kind.COMPUTER, COMPUTER_HEADERS);
    }

    public AssignmentImportPreview previewPhones(MultipartFile file) {
        return preview(file, AssignmentImportPreview.Kind.PHONE, PHONE_HEADERS);
    }

    private AssignmentImportPreview preview(
            MultipartFile file,
            AssignmentImportPreview.Kind kind,
            List<String> requiredHeaders
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un archivo CSV.");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("El archivo debe tener extensión .csv.");
        }
        try (PushbackReader reader = utf8Reader(file);
             CSVParser parser = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            validateHeaders(parser.getHeaderNames(), requiredHeaders);
            List<AssignmentImportPreview.Row> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException("La fila " + record.getRecordNumber() + " no tiene todas las columnas requeridas.");
                }
                if (Arrays.stream(record.values()).anyMatch(value -> !value.isBlank())) {
                    AssignmentImportPreview.Row row = new AssignmentImportPreview.Row(
                            required(record, requiredHeaders.getFirst(), record.getRecordNumber()),
                            required(record, "username", record.getRecordNumber()),
                            required(record, "assignment_type", record.getRecordNumber()),
                            required(record, "assigned_at", record.getRecordNumber()),
                            optional(record, "due_date"),
                            optional(record, "returned_at"),
                            optional(record, "notes")
                    );
                    validateRow(row, kind, record.getRecordNumber());
                    rows.add(row);
                }
            }
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("El CSV no contiene asignaciones para importar.");
            }
            return new AssignmentImportPreview(kind, List.copyOf(rows));
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible leer el CSV.", exception);
        }
    }

    private PushbackReader utf8Reader(MultipartFile file) throws IOException {
        PushbackReader reader = new PushbackReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8), 1);
        int first = reader.read();
        if (first != 0xFEFF && first != -1) reader.unread(first);
        return reader;
    }

    private void validateHeaders(List<String> actual, List<String> required) {
        List<String> normalized = actual;
        Set<String> actualSet = new LinkedHashSet<>(normalized);
        if (normalized.size() != actualSet.size() || !actualSet.equals(new LinkedHashSet<>(required))) {
            throw new IllegalArgumentException("Las columnas deben ser exactamente: " + String.join(", ", required) + ".");
        }
    }

    private String required(CSVRecord record, String header, long row) {
        String value = optional(record, header);
        if (value == null) {
            throw new IllegalArgumentException("La fila " + row + " no tiene " + header + ".");
        }
        return value;
    }

    private String optional(CSVRecord record, String header) {
        String value = record.get(header).trim();
        return value.isBlank() ? null : value;
    }

    private void validateRow(AssignmentImportPreview.Row row, AssignmentImportPreview.Kind kind, long number) {
        try {
            LocalDate assignedAt = LocalDate.parse(row.assignedAt());
            LocalDate dueAt = row.dueAt() == null ? null : LocalDate.parse(row.dueAt());
            LocalDate returnedAt = row.returnedAt() == null ? null : LocalDate.parse(row.returnedAt());
            boolean loan = "LOAN".equals(row.assignmentType());
            if (!(loan || "ASSIGNMENT".equals(row.assignmentType()))) {
                throw new IllegalArgumentException("assignment_type debe ser ASSIGNMENT o LOAN");
            }
            if (loan && dueAt == null) throw new IllegalArgumentException("un préstamo requiere due_date");
            if (!loan && dueAt != null) throw new IllegalArgumentException("due_date solo aplica a LOAN");
            if (dueAt != null && dueAt.isBefore(assignedAt)) throw new IllegalArgumentException("due_date no puede ser anterior a assigned_at");
            if (returnedAt != null && returnedAt.isBefore(assignedAt)) throw new IllegalArgumentException("returned_at no puede ser anterior a assigned_at");
            if (kind == AssignmentImportPreview.Kind.COMPUTER) {
                computerRepository.findByHostIgnoreCase(row.assetIdentifier()).orElseThrow(() -> new IllegalArgumentException("no existe el host " + row.assetIdentifier()));
            } else {
                phoneRepository.findByImeiIgnoreCase(row.assetIdentifier()).orElseThrow(() -> new IllegalArgumentException("no existe el IMEI " + row.assetIdentifier()));
            }
            personRepository.findByUsernameIgnoreCase(row.username()).orElseThrow(() -> new IllegalArgumentException("no existe el usuario " + row.username()));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fila " + number + " debe usar fechas yyyy-MM-dd.");
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("La fila " + number + ": " + exception.getMessage());
        }
    }

    @Transactional
    public void importPreview(AssignmentImportPreview preview) {
        if (preview.kind() == AssignmentImportPreview.Kind.COMPUTER) {
            importComputers(preview.rows());
        } else {
            importPhones(preview.rows());
        }
    }

    private void importComputers(List<AssignmentImportPreview.Row> rows) {
        Set<Computer> affected = new LinkedHashSet<>();
        for (AssignmentImportPreview.Row row : rows) {
            Computer computer = computerRepository.findByHostIgnoreCase(row.assetIdentifier()).orElseThrow();
            Person person = personRepository.findByUsernameIgnoreCase(row.username()).orElseThrow();
            if (computer.getStatus() == ComputerStatus.RETIRED) throw new IllegalArgumentException("No se puede asignar un equipo dado de baja.");
            if (row.returnedAt() == null && computerAssignmentRepository.existsByComputer_IdAndPerson_IdAndReturnedAtIsNull(computer.getId(), person.getId())) throw new IllegalArgumentException("Ya existe una asignación activa para " + computer.getHost() + " y " + person.getUsername() + ".");
            ComputerAssignment assignment = new ComputerAssignment(computer, person, AssignmentType.valueOf(row.assignmentType()), LocalDate.parse(row.assignedAt()), row.dueAt() == null ? null : LocalDate.parse(row.dueAt()), row.notes());
            if (row.returnedAt() != null) assignment.close(LocalDate.parse(row.returnedAt()));
            computerAssignmentRepository.save(assignment);
            affected.add(computer);
        }
        computerAssignmentService.refreshStatuses(affected);
    }

    private void importPhones(List<AssignmentImportPreview.Row> rows) {
        Set<Phone> affected = new LinkedHashSet<>();
        for (AssignmentImportPreview.Row row : rows) {
            Phone phone = phoneRepository.findByImeiIgnoreCase(row.assetIdentifier()).orElseThrow();
            Person person = personRepository.findByUsernameIgnoreCase(row.username()).orElseThrow();
            if (phone.getStatus() == PhoneStatus.RETIRED) throw new IllegalArgumentException("No se puede asignar un teléfono dado de baja.");
            if (row.returnedAt() == null && phoneAssignmentRepository.existsByPhoneIdAndPersonIdAndReturnedAtIsNull(phone.getId(), person.getId())) throw new IllegalArgumentException("Ya existe una asignación activa para " + phone.getImei() + " y " + person.getUsername() + ".");
            PhoneAssignment assignment = new PhoneAssignment(phone, person, PhoneAssignmentType.valueOf(row.assignmentType()), LocalDate.parse(row.assignedAt()), row.dueAt() == null ? null : LocalDate.parse(row.dueAt()), row.notes());
            if (row.returnedAt() != null) assignment.returnOn(LocalDate.parse(row.returnedAt()));
            phoneAssignmentRepository.save(assignment);
            affected.add(phone);
        }
        phoneAssignmentService.refreshStatuses(affected);
    }
}
