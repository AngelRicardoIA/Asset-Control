package com.assetcontrol.labels;

import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.labels.application.AssignmentLabelService;
import com.assetcontrol.labels.application.LabelContent;
import com.assetcontrol.labels.application.LabelPrinter;
import com.assetcontrol.labels.application.LabelPrintingException;
import com.assetcontrol.labels.application.LabelUsernameFormatter;
import com.assetcontrol.labels.infrastructure.TcpLabelPrinter;
import com.assetcontrol.labels.infrastructure.ZplLabelRenderer;
import com.assetcontrol.labels.web.AssignmentLabelController;
import com.assetcontrol.people.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssignmentLabelTests {

    private static final String ENDPOINT = "/computers/7/assignments/41/label";
    private static final String REQUEST = """
            {"printerIp":"192.168.1.30","content":{
            "displayName":"Nombre editado","asset":"A-EDITADO","model":"Modelo editado",
            "serialNumber":"SERIE-EDITADA","host":"HOST-EDITADO"}}
            """;

    private ComputerAssignmentRepository repository;
    private ComputerAssignment assignment;
    private Computer computer;
    private Person person;
    private LabelPrinter printer;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = mock(ComputerAssignmentRepository.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        assignment = mock(ComputerAssignment.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        computer = mock(Computer.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        person = mock(Person.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        printer = mock(LabelPrinter.class, withSettings().mockMaker(MockMakers.SUBCLASS));
        when(repository.findDetailedById(41L)).thenReturn(Optional.of(assignment));
        when(assignment.getComputer()).thenReturn(computer);
        when(assignment.getPerson()).thenReturn(person);
        when(assignment.isActive()).thenReturn(true);
        when(computer.getId()).thenReturn(7L);
        when(computer.getHost()).thenReturn("HOST-DEMO");
        when(computer.getAsset()).thenReturn("ASSET-DEMO");
        when(computer.getModel()).thenReturn("Modelo de prueba");
        when(computer.getSerialNumber()).thenReturn("SERIE-DEMO");
        when(person.getUsername()).thenReturn("angel.ibanez");
        var service = new AssignmentLabelService(
                repository,
                printer,
                new LabelUsernameFormatter(),
                "192.168.1.30"
        );
        mvc = MockMvcBuilders.standaloneSetup(new AssignmentLabelController(service)).build();
    }

    @Test
    void loadsFormattedUsernameWithHostAndConfiguredPrinter() throws Exception {
        mvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.printerIp").value("192.168.1.30"))
                .andExpect(jsonPath("$.content.displayName").value("Angel Ibanez"))
                .andExpect(jsonPath("$.content.host").value("HOST-DEMO"));
        verifyNoInteractions(printer);
        verify(person, never()).getFullName();
        verify(person, never()).getExternalId();
    }

    @Test
    void formatsUsernameSeparatorsAndRejectsMissingUsernames() throws Exception {
        assertEquals("Ana Maria Lopez", new LabelUsernameFormatter().format("ANA_maria.lopez"));
        assertEquals("Angel Ibanez", new LabelUsernameFormatter().format("angel.ibanez"));
        assertThrows(IllegalArgumentException.class, () -> new LabelUsernameFormatter().format("._-"));
        when(person.getUsername()).thenReturn(" ");
        mvc.perform(get(ENDPOINT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La persona asignada no tiene un nombre de usuario para la etiqueta."));
    }

    @Test
    void printsEditedValuesOnceWithoutUpdatingInventory() throws Exception {
        mvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isOk());
        verify(printer).print("192.168.1.30", new LabelContent(
                "Nombre editado", "A-EDITADO", "Modelo editado", "SERIE-EDITADA", "HOST-EDITADO"
        ));
        verify(repository, never()).save(any());
        verifyNoMoreInteractions(printer);
        mvc.perform(get(ENDPOINT))
                .andExpect(jsonPath("$.content.host").value("HOST-DEMO"))
                .andExpect(jsonPath("$.content.displayName").value("Angel Ibanez"));
    }

    @Test
    void rejectsReturnedUnknownAndMismatchedAssignmentsBeforePrinting() throws Exception {
        mvc.perform(get("/computers/8/assignments/41/label")).andExpect(status().isNotFound());
        mvc.perform(post("/computers/8/assignments/41/label")
                .contentType(MediaType.APPLICATION_JSON).content(REQUEST)).andExpect(status().isNotFound());
        mvc.perform(get("/computers/7/assignments/999/label")).andExpect(status().isNotFound());
        when(assignment.isActive()).thenReturn(false);
        mvc.perform(get(ENDPOINT)).andExpect(status().isNotFound());
        mvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isNotFound());
        verifyNoInteractions(printer);
    }

    @Test
    void rejectsMissingOversizedAndControlCharacterFields() throws Exception {
        for (String request : new String[] {
                REQUEST.replace("HOST-EDITADO", " "),
                REQUEST.replace("Nombre editado", "X".repeat(101)),
                REQUEST.replace("Nombre editado", "Nombre\\nOtra línea"),
                "{\"printerIp\":\"192.168.1.30\",\"content\":null}"
        }) {
            mvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
        verifyNoInteractions(printer);
    }

    @Test
    void reportsPrinterFailureWithoutRetrying() throws Exception {
        doThrow(new LabelPrintingException("No se pudo confirmar el envío.", new java.io.IOException()))
                .when(printer).print(anyString(), any());
        mvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(REQUEST))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("No se pudo confirmar el envío."));
        verify(printer, times(1)).print(anyString(), any());
    }

    @Test
    void usesLegacyCoordinatesAndEncodesTextAsLiteralData() {
        String input = "María ^XZ~JA_41\\& López";
        var content = new LabelContent(input, "00123", "Modelo", "Serie", "HOST-DEMO");
        String zpl = new String(renderer().render(content), StandardCharsets.US_ASCII);
        String encoded = HexFormat.ofDelimiter("_").withUpperCase()
                .formatHex(input.getBytes(StandardCharsets.UTF_8));

        assertTrue(zpl.contains("^MMT\n^PW531\n^LL0531\n^LS0"));
        assertTrue(zpl.contains("^FO24,40^GB489,418,6^FS"));
        assertTrue(zpl.contains("^FO28,176^GB482,0,7^FS"));
        assertTrue(zpl.contains("^FT56,232^A0N,33,33^FH_^FDName: _" + encoded + "^FS"));
        assertTrue(zpl.contains("^FT56,420^A0N,41,40^FH_^FDID: _48_4F_53_54_2D_44_45_4D_4F^FS"));
        assertEquals(1, zpl.split(Pattern.quote("^XZ"), -1).length - 1);
        assertFalse(zpl.contains("~JA"));
        assertFalse(zpl.contains("^GFA"));
        assertFalse(zpl.contains("^FB"));
    }

    @Test
    void acceptsBrandingFromAnExternalHeaderWithoutChangingThePublicLayout() {
        var header = new ByteArrayResource("^FO32,64^GFA,4,4,1,ABCD^FS".getBytes(StandardCharsets.UTF_8));
        String zpl = new String(renderer(header).render(new LabelContent(
                "Angel Ibanez", "ASSET-1", "Modelo", "Serie", "HOST-1"
        )), StandardCharsets.US_ASCII);

        assertTrue(zpl.contains("^FO32,64^GFA,4,4,1,ABCD^FS"));
        assertFalse(zpl.contains("Asset-Control"));
        assertTrue(zpl.contains("^FT56,232^A0N,33,33"));
    }

    @Test
    void sendsOneCompleteLabelToALocalTestSocketAndRejectsOtherDestinations() throws Exception {
        var content = new LabelContent("Angel Ibanez", "Asset", "Modelo", "Serie", "Host");
        var renderer = renderer();
        try (var server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
             var executor = Executors.newSingleThreadExecutor()) {
            server.setSoTimeout(3000);
            var received = executor.submit(() -> {
                try (var socket = server.accept()) {
                    socket.setSoTimeout(3000);
                    return socket.getInputStream().readAllBytes();
                }
            });
            new TcpLabelPrinter(renderer, server.getLocalPort(), 2000, "127.0.0.1")
                    .print("127.0.0.1", content);
            assertArrayEquals(renderer.render(content), received.get(3, TimeUnit.SECONDS));
        }
        var restricted = new TcpLabelPrinter(renderer, 9100, 1000, "192.168.1.30");
        for (String ip : new String[] {"localhost", "127.0.0.1", "169.254.169.254", "192.168.1.31", "256.1.1.1"}) {
            assertThrows(IllegalArgumentException.class, () -> restricted.print(ip, content));
        }
    }

    private ZplLabelRenderer renderer() {
        return renderer(new ClassPathResource("label-templates/generic-header.zpl"));
    }

    private ZplLabelRenderer renderer(ByteArrayResource header) {
        return new ZplLabelRenderer(new ClassPathResource("label-templates/legacy-layout.zpl"), header);
    }

    private ZplLabelRenderer renderer(ClassPathResource header) {
        return new ZplLabelRenderer(new ClassPathResource("label-templates/legacy-layout.zpl"), header);
    }
}
