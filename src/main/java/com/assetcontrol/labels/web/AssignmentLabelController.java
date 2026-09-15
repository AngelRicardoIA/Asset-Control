package com.assetcontrol.labels.web;

import com.assetcontrol.assignments.application.AssignmentNotFoundException;
import com.assetcontrol.labels.application.AssignmentLabelService;
import com.assetcontrol.labels.application.LabelContent;
import com.assetcontrol.labels.application.LabelPrintingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/computers/{computerId}/assignments/{assignmentId}/label")
public class AssignmentLabelController {

    private final AssignmentLabelService labelService;

    public AssignmentLabelController(AssignmentLabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public ResponseEntity<AssignmentLabelService.PreparedLabel> prepare(
            @PathVariable Long computerId,
            @PathVariable Long assignmentId
    ) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(labelService.prepare(computerId, assignmentId));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Map<String, String>> print(
            @PathVariable Long computerId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody PrintLabelRequest request
    ) {
        labelService.print(computerId, assignmentId, request.printerIp(), request.content());
        return message(HttpStatus.OK, "Etiqueta enviada a la impresora.");
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> assignmentUnavailable() {
        return message(HttpStatus.NOT_FOUND, "La asignación ya no está activa o no corresponde a este equipo. Actualiza la página.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidForm(MethodArgumentNotValidException exception) {
        String error = exception.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return message(HttpStatus.BAD_REQUEST, error == null ? "Revisa los datos de la etiqueta." : error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidPrinter(IllegalArgumentException exception) {
        return message(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(LabelPrintingException.class)
    public ResponseEntity<Map<String, String>> printingFailed(LabelPrintingException exception) {
        return message(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    private ResponseEntity<Map<String, String>> message(HttpStatus status, String message) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(Map.of("message", message));
    }

    public record PrintLabelRequest(
            @NotBlank(message = "Escribe la IP de la impresora.")
            @Size(max = 15, message = "Escribe una dirección IPv4 válida.")
            String printerIp,
            @NotNull(message = "Faltan los datos de la etiqueta.") @Valid LabelContent content
    ) {
    }
}
