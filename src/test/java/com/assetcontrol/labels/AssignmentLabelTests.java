package com.assetcontrol.labels;

import com.assetcontrol.assignments.domain.ComputerAssignment;
import com.assetcontrol.assignments.domain.ComputerAssignmentRepository;
import com.assetcontrol.computers.domain.Computer;
import com.assetcontrol.labels.application.AssignmentLabelService;
import com.assetcontrol.labels.application.LabelContent;
import com.assetcontrol.labels.application.LabelPrinter;
import com.assetcontrol.labels.application.LabelPrintingException;
import com.assetcontrol.labels.infrastructure.TcpLabelPrinter;
import com.assetcontrol.labels.infrastructure.ZplLabelRenderer;
import com.assetcontrol.labels.web.AssignmentLabelController;
import com.assetcontrol.people.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AssignmentLabelTests {

    private static final String ENDPOINT = "/computers/7/assignments/41/label";
    private static final String REQUEST = """
            {"printerIp":"192.168.1.30","content":{
            "fullName":"Nombre editado","asset":"A-EDITADO","model":"Modelo editado",
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
        when(person.getFullName()).thenReturn("María López");
        when(person.getExternalId()).thenReturn("00047");
        var service = new AssignmentLabelService(repository, printer, "192.168.1.30");
        mvc = MockMvcBuilders.standaloneSetup(new AssignmentLabelController(service)).build();
    }

    @Test
    void loadsCurrentAssignmentWithHostAndConfiguredPrinter() throws Exception {
        mvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.printerIp").value("192.168.1.30"))
                .andExpect(jsonPath("$.content.fullName").value("María López"))
                .andExpect(jsonPath("$.content.host").value("HOST-DEMO"));
        verifyNoInteractions(printer);
        verify(person, never()).getExternalId();
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
                .andExpect(jsonPath("$.content.host").value("HOST-DEMO"));
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
    void encodesAccentsAndZplCommandsAsLiteralText() {
        String input = "María ^XZ~JA_41\\& López";
        var content = new LabelContent(input, "00123", "Modelo", "Serie", "HOST-DEMO");
        String zpl = new String(new ZplLabelRenderer().render(content), StandardCharsets.US_ASCII);
        assertTrue(zpl.contains("^CI28\n"));
        assertEquals(1, zpl.split(Pattern.quote("^XZ"), -1).length - 1);
        assertFalse(zpl.contains("~JA"));
        assertFalse(zpl.contains("^GFA"));
        var fields = Pattern.compile("\\^FH_\\^FD((?:_[0-9A-F]{2})+)\\^FS").matcher(zpl);
        boolean found = false;
        while (fields.find()) {
            byte[] bytes = HexFormat.of().parseHex(fields.group(1).replace("_", ""));
            if (new String(bytes, StandardCharsets.UTF_8).equals(input)) found = true;
        }
        assertTrue(found);
    }

    @Test
    void sendsOneCompleteLabelToALocalTestSocketAndRejectsOtherDestinations() throws Exception {
        var content = new LabelContent("Nombre", "Asset", "Modelo", "Serie", "Host");
        var renderer = new ZplLabelRenderer();
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
}
