package com.assetcontrol.imports.application;

import com.assetcontrol.assignments.application.ComputerAssignmentService;
import com.assetcontrol.assignments.domain.AssignmentType;
import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.computers.domain.ComputerRepository;
import com.assetcontrol.computers.domain.ComputerStatus;
import com.assetcontrol.computers.domain.ComputerType;
import com.assetcontrol.people.domain.Person;
import com.assetcontrol.people.domain.PersonRepository;
import com.assetcontrol.sites.domain.Site;
import com.assetcontrol.sites.domain.SiteRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ComputerInventoryCsvImportService {

    public static final List<String> HEADERS = List.of(
            "Localidad", "No. Empleado", "Nombre", "Puesto", "Area", "Gerente",
            "User", "Correo", "Tipo", "Host", "Marca", "Modelo", "Asset",
            "No. Serie", "S.O.", "Cargador", "Fecha asignación"
    );

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ROOT),
            DateTimeFormatter.ofPattern("d-M-uuuu", Locale.ROOT)
    );

    private final SiteRepository siteRepository;
    private final PersonRepository personRepository;
    private final ComputerRepository computerRepository;
    private final ComputerAssignmentRepository assignmentRepository;
    private final ComputerAssignmentService assignmentService;

    public ComputerInventoryCsvImportService(
            SiteRepository siteRepository,
            PersonRepository personRepository,
            ComputerRepository computerRepository,
            ComputerAssignmentRepository assignmentRepository,
            ComputerAssignmentService assignmentService
    ) {
        this.siteRepository = siteRepository;
        this.personRepository = personRepository;
        this.computerRepository = computerRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentService = assignmentService;
    }

    public ComputerInventoryImportPreview preview(MultipartFile file) {
        validateFile(file);

        try (
                PushbackReader reader = utf8Reader(file);
                CSVParser parser = CSVFormat.RFC4180.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .build()
                        .parse(reader)
        ) {
            validateHeaders(parser.getHeaderNames());

            List<ComputerInventoryImportRow> rows = new ArrayList<>();
            Set<String> assignmentKeys = new LinkedHashSet<>();

            for (CSVRecord record : parser) {
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException(
                            "La fila " + record.getRecordNumber()
                                    + " no tiene todas las columnas requeridas."
                    );
                }

                if (Arrays.stream(record.values()).allMatch(String::isBlank)) {
                    continue;
                }

                ComputerInventoryImportRow row = toRow(record);
                validateRow(row, record.getRecordNumber());

                String assignmentKey = row.host().toLowerCase(Locale.ROOT)
                        + "|" + row.username().toLowerCase(Locale.ROOT);

                if (!assignmentKeys.add(assignmentKey)) {
                    throw new IllegalArgumentException(
                            "La fila " + record.getRecordNumber()
                                    + " duplica la asignación de " + row.host()
                                    + " a " + row.username() + "."
                    );
                }

                rows.add(row);
            }

            if (rows.isEmpty()) {
                throw new IllegalArgumentException(
                        "El CSV no contiene equipos para importar."
                );
            }

            return new ComputerInventoryImportPreview(List.copyOf(rows));
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "No fue posible leer el CSV.",
                    exception
            );
        }
    }

    @Transactional
    public void importPreview(ComputerInventoryImportPreview preview) {
        Set<Computer> affectedComputers = new LinkedHashSet<>();

        for (ComputerInventoryImportRow row : preview.rows()) {
            Site site = resolveSite(row.siteName());
            Person person = resolvePerson(row);
            Computer computer = resolveComputer(row, site);

            if (computer.getStatus() == ComputerStatus.RETIRED) {
                throw new IllegalArgumentException(
                        "No se puede importar una asignación para el equipo dado de baja "
                                + computer.getHost() + "."
                );
            }

            if (!assignmentRepository.existsByComputer_IdAndPerson_IdAndReturnedAtIsNull(
                    computer.getId(),
                    person.getId()
            )) {
                assignmentRepository.save(new ComputerAssignment(
                        computer,
                        person,
                        AssignmentType.ASSIGNMENT,
                        row.assignedAt(),
                        null,
                        null
                ));
            }

            affectedComputers.add(computer);
        }

        assignmentService.refreshStatuses(affectedComputers);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un archivo CSV.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IllegalArgumentException(
                    "El archivo debe tener extensión .csv."
            );
        }
    }

    private PushbackReader utf8Reader(MultipartFile file) throws IOException {
        PushbackReader reader = new PushbackReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
                1
        );
        int first = reader.read();
        if (first != 0xFEFF && first != -1) {
            reader.unread(first);
        }
        return reader;
    }

    private void validateHeaders(List<String> headers) {
        Set<String> actualHeaders = new LinkedHashSet<>(headers);
        if (headers.size() != actualHeaders.size()
                || !actualHeaders.equals(new LinkedHashSet<>(HEADERS))) {
            throw new IllegalArgumentException(
                    "Las columnas deben ser exactamente: "
                            + String.join(", ", HEADERS) + "."
            );
        }
    }

    private ComputerInventoryImportRow toRow(CSVRecord record) {
        return new ComputerInventoryImportRow(
                required(record, "Localidad"),
                required(record, "No. Empleado"),
                required(record, "Nombre"),
                optional(record, "Puesto"),
                optional(record, "Area"),
                optional(record, "Gerente"),
                required(record, "User"),
                required(record, "Correo"),
                required(record, "Tipo"),
                required(record, "Host"),
                required(record, "Marca"),
                required(record, "Modelo"),
                required(record, "Asset"),
                required(record, "No. Serie"),
                optional(record, "S.O."),
                optional(record, "Cargador"),
                parseDate(required(record, "Fecha asignación"))
        );
    }

    private String required(CSVRecord record, String header) {
        String value = optional(record, header);
        if (value == null) {
            throw new IllegalArgumentException(
                    "La fila " + record.getRecordNumber()
                            + " no tiene " + header + "."
            );
        }
        return value;
    }

    private String optional(CSVRecord record, String header) {
        String value = record.get(header).strip().replaceAll("\\s+", " ");
        return value.isBlank() ? null : value;
    }

    private LocalDate parseDate(String value) {
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException(
                "Fecha asignación debe usar yyyy-MM-dd o d/M/yyyy."
        );
    }

    private void validateRow(ComputerInventoryImportRow row, long number) {
        try {
            computerType(row.computerType());

            Optional<Computer> byHost = computerRepository.findByHostIgnoreCase(row.host());
            Optional<Computer> byAsset = computerRepository.findByAssetIgnoreCase(row.asset());
            if (byHost.isPresent() && byAsset.isPresent()
                    && !byHost.get().getId().equals(byAsset.get().getId())) {
                throw new IllegalArgumentException(
                        "HOST y Asset pertenecen a equipos distintos."
                );
            }

            Optional<Person> byEmployee = personRepository.findByExternalIdIgnoreCase(
                    row.employeeNumber()
            );
            Optional<Person> byUsername = personRepository.findByUsernameIgnoreCase(
                    row.username()
            );
            if (byEmployee.isPresent() && byUsername.isPresent()
                    && !byEmployee.get().getId().equals(byUsername.get().getId())) {
                throw new IllegalArgumentException(
                        "No. Empleado y User pertenecen a personas distintas."
                );
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "La fila " + number + ": " + exception.getMessage()
            );
        }
    }

    private Site resolveSite(String name) {
        Site site = siteRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> siteRepository.save(new Site(name)));

        if (!site.isActive()) {
            site.activate();
        }

        return site;
    }

    private Person resolvePerson(ComputerInventoryImportRow row) {
        Person person = personRepository.findByExternalIdIgnoreCase(
                        row.employeeNumber()
                )
                .or(() -> personRepository.findByUsernameIgnoreCase(row.username()))
                .orElseGet(() -> personRepository.save(new Person(
                        row.employeeNumber(),
                        row.username(),
                        row.fullName(),
                        row.email()
                )));

        person.updateDetails(
                row.employeeNumber(),
                row.username(),
                row.fullName(),
                row.email(),
                row.jobTitle(),
                row.department(),
                row.managerName()
        );

        return person;
    }

    private Computer resolveComputer(
            ComputerInventoryImportRow row,
            Site site
    ) {
        Computer computer = computerRepository.findByHostIgnoreCase(row.host())
                .or(() -> computerRepository.findByAssetIgnoreCase(row.asset()))
                .orElseGet(() -> computerRepository.save(new Computer(
                        row.asset(),
                        row.host(),
                        computerType(row.computerType()),
                        row.brand(),
                        row.model(),
                        row.serialNumber(),
                        row.chargerSerialNumber(),
                        row.operatingSystem(),
                        ComputerStatus.AVAILABLE,
                        site,
                        null
                )));

        computer.updateDetails(
                row.asset(),
                row.host(),
                computerType(row.computerType()),
                row.brand(),
                row.model(),
                row.serialNumber(),
                row.chargerSerialNumber(),
                row.operatingSystem(),
                site,
                computer.getObservations()
        );

        return computer;
    }

    private ComputerType computerType(String value) {
        String normalized = value.strip().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "LAPTOP" -> ComputerType.LAPTOP;
            case "PC", "DESKTOP", "EQUIPO DE ESCRITORIO" -> ComputerType.DESKTOP;
            default -> throw new IllegalArgumentException(
                    "Tipo debe ser Laptop, PC o Equipo de escritorio."
            );
        };
    }
}
