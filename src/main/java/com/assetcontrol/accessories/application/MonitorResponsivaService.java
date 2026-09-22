package com.assetcontrol.accessories.application;

import com.assetcontrol.accessories.domain.AccessoryAssignmentRepository;
import com.assetcontrol.accessories.domain.AccessoryType;
import com.assetcontrol.documents.application.GeneratedResponsiva;
import com.assetcontrol.documents.application.ResponsivaGenerationException;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class MonitorResponsivaService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final AccessoryAssignmentRepository assignments;

    public MonitorResponsivaService(AccessoryAssignmentRepository assignments) {
        this.assignments = assignments;
    }

    @Transactional(readOnly = true)
    public GeneratedResponsiva generate(Long accessoryId, Long assignmentId) {
        var assignment = assignments.findByIdAndAccessory_Id(assignmentId, accessoryId)
                .filter(item -> item.isActive() && item.getAccessory().getType() == AccessoryType.MONITOR)
                .orElseThrow(() -> new IllegalArgumentException("No hay una asignación activa de monitor."));
        var monitor = assignment.getAccessory();
        var person = assignment.getPerson();
        try (var document = new XWPFDocument(); var output = new ByteArrayOutputStream()) {
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            var titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(15);
            titleRun.setText("CARTA DE ASIGNACIÓN Y ACEPTACIÓN DE MONITOR");

            XWPFParagraph intro = document.createParagraph();
            intro.setSpacingBefore(500);
            intro.setSpacingAfter(350);
            intro.createRun().setText("Se hace constar la entrega del monitor detallado a continuación. La persona asignada acepta su resguardo y uso conforme a las políticas aplicables.");

            var table = document.createTable(7, 2);
            field(table, 0, "Nombre completo", person.getFullName());
            field(table, 1, "Número de empleado", person.getExternalId());
            field(table, 2, "Marca", monitor.getBrand());
            field(table, 3, "Modelo", monitor.getModel());
            field(table, 4, "Número de serie", fallback(monitor.getSerialNumber()));
            field(table, 5, "Asset", fallback(monitor.getAsset()));
            field(table, 6, "Fecha de asignación", assignment.getAssignedAt().format(DATE));

            signature(document, "Firma de quien recibe: _______________________________");
            signature(document, "Firma de quien entrega: _______________________________");
            document.write(output);
            return new GeneratedResponsiva("responsiva-monitor-" + accessoryId + "-asignacion-" + assignmentId + ".docx", output.toByteArray());
        } catch (IOException exception) {
            throw new ResponsivaGenerationException("No se pudo generar la responsiva del monitor.", exception);
        }
    }

    private void field(org.apache.poi.xwpf.usermodel.XWPFTable table, int row, String name, String value) {
        table.getRow(row).getCell(0).setText(name);
        table.getRow(row).getCell(1).setText(value);
    }

    private void signature(XWPFDocument document, String text) {
        var paragraph = document.createParagraph();
        paragraph.setSpacingBefore(650);
        paragraph.createRun().setText(text);
    }

    private String fallback(String value) {
        return value == null || value.isBlank() ? "No registrado" : value;
    }
}
