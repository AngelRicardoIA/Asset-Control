package com.assetcontrol.documents;

import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.documents.application.AssignmentResponsivaService;
import com.assetcontrol.documents.application.ResponsivaGenerationException;
import com.assetcontrol.documents.infrastructure.PoiDocxTemplateRenderer;
import com.assetcontrol.documents.web.AssignmentResponsivaController;
import com.assetcontrol.people.domain.Person;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AssignmentResponsivaTests {

    private static final String ENDPOINT = "/computers/7/assignments/41/responsiva.docx";

    @Test
    void rendersSplitRepeatedMarkersAndKeepsRunFormatting() throws Exception {
        byte[] template;
        try (var document = new XWPFDocument(); var output = new ByteArrayOutputStream()) {
            var paragraph = document.createParagraph();
            var first = paragraph.createRun();
            first.setBold(true);
            first.setText("Nombre: {{emplo");
            first.setText("yee.");
            var middle = paragraph.createRun();
            middle.setItalic(true);
            middle.setText("fullName}} y {{employee.full");
            var last = paragraph.createRun();
            last.setText("Name}}.");
            document.write(output);
            template = output.toByteArray();
        }

        byte[] result = renderer(template).render(Map.of("employee.fullName", "María & José"));
        try (var document = new XWPFDocument(new ByteArrayInputStream(result))) {
            var paragraph = document.getParagraphs().getFirst();
            assertEquals("Nombre: María & José y María & José.", paragraph.getText());
            assertTrue(paragraph.getRuns().get(0).isBold());
            assertTrue(paragraph.getRuns().get(1).isItalic());
        }
    }

    @Test
    void rendersTablesHeadersAndFootersWithoutReinterpretingValues() throws Exception {
        byte[] template;
        try (var document = new XWPFDocument(); var output = new ByteArrayOutputStream()) {
            document.createTable(1, 1).getRow(0).getCell(0).setText("{{employee.fullName}}");
            document.createHeader(HeaderFooterType.DEFAULT).createParagraph()
                    .createRun().setText("{{computer.asset}}");
            document.createFooter(HeaderFooterType.DEFAULT).createParagraph()
                    .createRun().setText("{{assignment.assignedAt}}");
            document.write(output);
            template = output.toByteArray();
        }

        byte[] result = renderer(template).render(Map.of(
                "employee.fullName", "Texto {{computer.asset}} & prueba",
                "computer.asset", "A-001",
                "assignment.assignedAt", "14/09/2026"
        ));
        try (var document = new XWPFDocument(new ByteArrayInputStream(result))) {
            assertEquals("Texto {{computer.asset}} & prueba", document.getTables().getFirst()
                    .getRow(0).getCell(0).getText());
            assertTrue(document.getHeaderList().getFirst().getText().contains("A-001"));
            assertTrue(document.getFooterList().getFirst().getText().contains("14/09/2026"));
        }
    }

    @Test
    void rejectsUnknownAndMissingMarkersAndUnavailableTemplates() throws Exception {
        assertThrows(ResponsivaGenerationException.class,
                () -> renderer(template("{{employee.typo}}"))
                        .render(Map.of("employee.fullName", "María")));
        assertThrows(ResponsivaGenerationException.class,
                () -> renderer(template("Sin marcadores"))
                        .render(Map.of("employee.fullName", "María")));
        assertThrows(ResponsivaGenerationException.class,
                () -> new PoiDocxTemplateRenderer(new FileSystemResource("missing-template.docx"))
                        .render(Map.of()));
    }

    @Test
    void downloadsSixMappedFieldsAndCanRepeatWithoutSavingAssignments() throws Exception {
        var repository = assignmentRepository();
        var controller = controller(repository, renderer(template(
                "{{employee.fullName}} | {{employee.number}} | {{computer.model}} | "
                        + "{{computer.asset}} | {{computer.serialNumber}} | {{assignment.assignedAt}}"
        )));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();

        for (int index = 0; index < 2; index++) {
            byte[] result = mvc.perform(get(ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"responsiva-HOST-DEMO-asignacion-41.docx\""))
                    .andExpect(header().string("Cache-Control", "no-store"))
                    .andReturn().getResponse().getContentAsByteArray();
            try (var document = new XWPFDocument(new ByteArrayInputStream(result));
                 var extractor = new XWPFWordExtractor(document)) {
                assertTrue(extractor.getText().contains(
                        "María & José | 00047 | Modelo de prueba | ASSET-DEMO | NS-DEMO | 14/09/2026"
                ));
                assertFalse(extractor.getText().contains("{{"));
            }
        }
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsUnknownAssignmentsAndAssignmentsFromAnotherComputer() throws Exception {
        var repository = assignmentRepository();
        var mvc = MockMvcBuilders.standaloneSetup(controller(repository,
                renderer(template("{{employee.fullName}}")))).build();

        mvc.perform(get("/computers/8/assignments/41/responsiva.docx"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/computers/7/assignments/999/responsiva.docx"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsAUsefulErrorWhenTheDocumentCannotBeGenerated() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(controller(assignmentRepository(),
                renderer(template("{{employee.typo}}")))).build();

        mvc.perform(get(ENDPOINT))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("assignments/responsiva-error"))
                .andExpect(model().attributeExists("message"));
    }

    private AssignmentResponsivaController controller(
            ComputerAssignmentRepository repository,
            PoiDocxTemplateRenderer renderer
    ) {
        return new AssignmentResponsivaController(new AssignmentResponsivaService(repository, renderer));
    }

    private ComputerAssignmentRepository assignmentRepository() {
        var repository = mock(ComputerAssignmentRepository.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        var assignment = mock(ComputerAssignment.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        var computer = mock(Computer.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        var person = mock(Person.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        when(repository.findDetailedById(41L)).thenReturn(Optional.of(assignment));
        when(assignment.getId()).thenReturn(41L);
        when(assignment.isActive()).thenReturn(true);
        when(assignment.getComputer()).thenReturn(computer);
        when(assignment.getPerson()).thenReturn(person);
        when(assignment.getAssignedAt()).thenReturn(LocalDate.of(2026, 9, 14));
        when(computer.getId()).thenReturn(7L);
        when(computer.getHost()).thenReturn("HOST-DEMO");
        when(computer.getModel()).thenReturn("Modelo de prueba");
        when(computer.getAsset()).thenReturn("ASSET-DEMO");
        when(computer.getSerialNumber()).thenReturn("NS-DEMO");
        when(person.getId()).thenReturn(3L);
        when(person.getExternalId()).thenReturn("00047");
        when(person.getFullName()).thenReturn("María & José");
        return repository;
    }

    private PoiDocxTemplateRenderer renderer(byte[] bytes) {
        return new PoiDocxTemplateRenderer(new ByteArrayResource(bytes));
    }

    private byte[] template(String text) throws Exception {
        try (var document = new XWPFDocument(); var output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(text);
            document.write(output);
            return output.toByteArray();
        }
    }
}
