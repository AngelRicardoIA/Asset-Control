package com.assetcontrol.documents.web;

import com.assetcontrol.assignments.application.AssignmentNotFoundException;
import com.assetcontrol.documents.application.AssignmentResponsivaService;
import com.assetcontrol.documents.application.ResponsivaGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("/computers/{computerId}/assignments/{assignmentId}")
public class AssignmentResponsivaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentResponsivaController.class);
    private static final MediaType DOCX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final AssignmentResponsivaService responsivaService;

    public AssignmentResponsivaController(AssignmentResponsivaService responsivaService) {
        this.responsivaService = responsivaService;
    }

    @GetMapping("/responsiva.docx")
    public ResponseEntity<byte[]> download(
            @PathVariable Long computerId,
            @PathVariable Long assignmentId
    ) {
        var document = responsivaService.generate(computerId, assignmentId);
        return ResponseEntity.ok()
                .contentType(DOCX)
                .contentLength(document.content().length)
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.filename()).build().toString()
                )
                .body(document.content());
    }

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ModelAndView notFound(AssignmentNotFoundException exception) {
        return new ModelAndView(
                "assignments/responsiva-error",
                Map.of("message", "No se encontró esa asignación para el equipo indicado."),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ResponsivaGenerationException.class)
    public ModelAndView generationFailed(ResponsivaGenerationException exception) {
        LOGGER.error("No se pudo generar la responsiva de asignación.", exception);
        return new ModelAndView(
                "assignments/responsiva-error",
                Map.of("message", exception.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
