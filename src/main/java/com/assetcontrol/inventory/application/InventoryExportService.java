package com.assetcontrol.inventory.application;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class InventoryExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(
            "dd/MM/yyyy"
    );

    private final JdbcTemplate jdbcTemplate;

    public InventoryExportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public byte[] export() {
        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            CellStyle headerStyle = headerStyle(workbook);

            sheets().forEach(definition -> writeSheet(
                    workbook,
                    definition,
                    jdbcTemplate.queryForList(definition.query()),
                    headerStyle
            ));

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No fue posible generar el archivo de inventario.",
                    exception
            );
        }
    }

    private List<SheetDefinition> sheets() {
        return List.of(
                new SheetDefinition(
                        "Equipos de cómputo",
                        """
                                SELECT s.name AS 'Ubicación', c.host AS 'HOST', c.asset AS 'Asset', c.computer_type AS 'Tipo', c.brand AS 'Marca', c.model AS 'Modelo', c.serial_number AS 'No. serie', c.operating_system AS 'S.O.', c.charger_serial_number AS 'Cargador', c.status AS 'Estado', c.observations AS 'Observaciones', p.external_id AS 'No. empleado', p.full_name AS 'Nombre completo', p.username AS 'Usuario', p.email AS 'Correo', p.job_title AS 'Puesto', p.department AS 'Área o departamento', p.manager_name AS 'Gerente', ca.assignment_type AS 'Tipo asignación', ca.assigned_at AS 'Fecha asignación', ca.due_date AS 'Vencimiento', ca.notes AS 'Observaciones asignación'
                                FROM computers c
                                JOIN sites s ON s.id = c.site_id
                                LEFT JOIN computer_assignments ca ON ca.computer_id = c.id AND ca.returned_at IS NULL
                                LEFT JOIN people p ON p.id = ca.person_id
                                ORDER BY c.host, p.username
                                """,
                        List.of("Fecha asignación", "Vencimiento")
                ),
                new SheetDefinition(
                        "Historial equipos",
                        """
                                SELECT s.name AS 'Ubicación', c.host AS 'HOST', c.asset AS 'Asset', c.brand AS 'Marca', c.model AS 'Modelo', c.serial_number AS 'No. serie', p.external_id AS 'No. empleado', p.full_name AS 'Nombre completo', p.username AS 'Usuario', p.email AS 'Correo', p.job_title AS 'Puesto', p.department AS 'Área o departamento', p.manager_name AS 'Gerente', ca.assignment_type AS 'Tipo asignación', ca.assigned_at AS 'Fecha asignación', ca.due_date AS 'Vencimiento', ca.returned_at AS 'Fecha devolución', ca.notes AS 'Observaciones'
                                FROM computer_assignments ca
                                JOIN computers c ON c.id = ca.computer_id
                                JOIN sites s ON s.id = c.site_id
                                JOIN people p ON p.id = ca.person_id
                                ORDER BY c.host, ca.assigned_at DESC
                                """,
                        List.of("Fecha asignación", "Vencimiento", "Fecha devolución")
                ),
                new SheetDefinition(
                        "Teléfonos",
                        """
                                SELECT s.name AS 'Ubicación', ph.imei AS 'IMEI', ph.brand AS 'Marca', ph.model AS 'Modelo', pl.number AS 'Línea', pl.carrier AS 'Operador', ph.status AS 'Estado', ph.observations AS 'Observaciones', p.external_id AS 'No. empleado', p.full_name AS 'Nombre completo', p.username AS 'Usuario', p.email AS 'Correo', p.job_title AS 'Puesto', p.department AS 'Área o departamento', p.manager_name AS 'Gerente', pa.assignment_type AS 'Tipo asignación', pa.assigned_at AS 'Fecha asignación', pa.due_at AS 'Vencimiento', pa.observations AS 'Observaciones asignación'
                                FROM phones ph
                                JOIN sites s ON s.id = ph.site_id
                                LEFT JOIN phone_lines pl ON pl.id = ph.phone_line_id
                                LEFT JOIN phone_assignments pa ON pa.phone_id = ph.id AND pa.returned_at IS NULL
                                LEFT JOIN people p ON p.id = pa.person_id
                                ORDER BY ph.imei, p.username
                                """,
                        List.of("Fecha asignación", "Vencimiento")
                ),
                new SheetDefinition(
                        "Historial teléfonos",
                        """
                                SELECT ph.imei AS 'IMEI', ph.brand AS 'Marca', ph.model AS 'Modelo', pl.number AS 'Línea', p.external_id AS 'No. empleado', p.full_name AS 'Nombre completo', p.username AS 'Usuario', p.email AS 'Correo', p.job_title AS 'Puesto', p.department AS 'Área o departamento', p.manager_name AS 'Gerente', pa.assignment_type AS 'Tipo asignación', pa.assigned_at AS 'Fecha asignación', pa.due_at AS 'Vencimiento', pa.returned_at AS 'Fecha devolución', pa.observations AS 'Observaciones'
                                FROM phone_assignments pa
                                JOIN phones ph ON ph.id = pa.phone_id
                                LEFT JOIN phone_lines pl ON pl.id = ph.phone_line_id
                                JOIN people p ON p.id = pa.person_id
                                ORDER BY ph.imei, pa.assigned_at DESC
                                """,
                        List.of("Fecha asignación", "Vencimiento", "Fecha devolución")
                ),
                new SheetDefinition(
                        "Mantenimientos equipos",
                        """
                                SELECT c.host AS 'HOST', c.asset AS 'Asset', c.brand AS 'Marca', c.model AS 'Modelo', mr.record_type AS 'Tipo', mr.performed_at AS 'Fecha', mr.description AS 'Descripción', mr.performed_by AS 'Realizado por'
                                FROM computer_maintenance_records mr
                                JOIN computers c ON c.id = mr.computer_id
                                ORDER BY mr.performed_at DESC, c.host
                                """,
                        List.of("Fecha")
                ),
                new SheetDefinition(
                        "Personas",
                        """
                                SELECT external_id AS 'No. empleado', full_name AS 'Nombre completo', username AS 'Usuario', email AS 'Correo', job_title AS 'Puesto', department AS 'Área o departamento', manager_name AS 'Gerente'
                                FROM people
                                ORDER BY full_name
                                """,
                        List.of()
                ),
                new SheetDefinition(
                        "Ubicaciones",
                        """
                                SELECT name AS 'Ubicación', CASE WHEN is_active THEN 'Activa' ELSE 'Inactiva' END AS 'Estado'
                                FROM sites
                                ORDER BY name
                                """,
                        List.of()
                ),
                new SheetDefinition(
                        "Líneas telefónicas",
                        """
                                SELECT number AS 'Línea', carrier AS 'Operador'
                                FROM phone_lines
                                ORDER BY number
                                """,
                        List.of()
                )
        );
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        return style;
    }

    private void writeSheet(
            XSSFWorkbook workbook,
            SheetDefinition definition,
            List<Map<String, Object>> rows,
            CellStyle headerStyle
    ) {
        Sheet sheet = workbook.createSheet(definition.name());
        List<String> headers = definition.headers();
        Row header = sheet.createRow(0);

        for (int column = 0; column < headers.size(); column++) {
            Cell cell = header.createCell(column);
            cell.setCellValue(headers.get(column));
            cell.setCellStyle(headerStyle);
        }

        for (int index = 0; index < rows.size(); index++) {
            Row row = sheet.createRow(index + 1);
            Map<String, Object> values = rows.get(index);

            for (int column = 0; column < headers.size(); column++) {
                String headerName = headers.get(column);
                row.createCell(column).setCellValue(format(
                        values.get(headerName),
                        definition.dateHeaders().contains(headerName)
                ));
            }
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                0,
                Math.max(0, rows.size()),
                0,
                Math.max(0, headers.size() - 1)
        ));

        for (int column = 0; column < headers.size(); column++) {
            sheet.autoSizeColumn(column);
            sheet.setColumnWidth(
                    column,
                    Math.min(sheet.getColumnWidth(column) + 512, 18000)
            );
        }
    }

    private String format(Object value, boolean date) {
        if (value == null) {
            return "";
        }

        String formatted = date ? formatDate(value) : String.valueOf(value);
        return formatted.startsWith("=")
                || formatted.startsWith("+")
                || formatted.startsWith("-")
                || formatted.startsWith("@")
                ? "'" + formatted
                : formatted;
    }

    private String formatDate(Object value) {
        if (value instanceof Number number) {
            return Instant.ofEpochMilli(number.longValue())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .format(DATE_FORMAT);
        }

        String text = String.valueOf(value);
        try {
            return LocalDate.parse(text).format(DATE_FORMAT);
        } catch (RuntimeException ignored) {
            return text;
        }
    }

    private record SheetDefinition(
            String name,
            String query,
            List<String> dateHeaders
    ) {
        private List<String> headers() {
            return switch (name) {
                case "Equipos de cómputo" -> List.of(
                        "Ubicación", "HOST", "Asset", "Tipo", "Marca", "Modelo",
                        "No. serie", "S.O.", "Cargador", "Estado", "Observaciones",
                        "No. empleado", "Nombre completo", "Usuario", "Correo", "Puesto",
                        "Área o departamento", "Gerente", "Tipo asignación",
                        "Fecha asignación", "Vencimiento", "Observaciones asignación"
                );
                case "Historial equipos" -> List.of(
                        "Ubicación", "HOST", "Asset", "Marca", "Modelo", "No. serie",
                        "No. empleado", "Nombre completo", "Usuario", "Correo", "Puesto",
                        "Área o departamento", "Gerente", "Tipo asignación",
                        "Fecha asignación", "Vencimiento", "Fecha devolución", "Observaciones"
                );
                case "Teléfonos" -> List.of(
                        "Ubicación", "IMEI", "Marca", "Modelo", "Línea", "Operador",
                        "Estado", "Observaciones", "No. empleado", "Nombre completo",
                        "Usuario", "Correo", "Puesto", "Área o departamento", "Gerente",
                        "Tipo asignación", "Fecha asignación", "Vencimiento",
                        "Observaciones asignación"
                );
                case "Historial teléfonos" -> List.of(
                        "IMEI", "Marca", "Modelo", "Línea", "No. empleado",
                        "Nombre completo", "Usuario", "Correo", "Puesto",
                        "Área o departamento", "Gerente", "Tipo asignación",
                        "Fecha asignación", "Vencimiento", "Fecha devolución", "Observaciones"
                );
                case "Mantenimientos equipos" -> List.of(
                        "HOST", "Asset", "Marca", "Modelo", "Tipo", "Fecha",
                        "Descripción", "Realizado por"
                );
                case "Personas" -> List.of(
                        "No. empleado", "Nombre completo", "Usuario", "Correo", "Puesto",
                        "Área o departamento", "Gerente"
                );
                case "Ubicaciones" -> List.of("Ubicación", "Estado");
                default -> List.of("Línea", "Operador");
            };
        }
    }
}
