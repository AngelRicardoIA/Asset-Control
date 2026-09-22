package com.assetcontrol.accessories.web;

import com.assetcontrol.accessories.application.MonitorResponsivaService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/accessories/{accessoryId}/assignments/{assignmentId}")
public class MonitorResponsivaController {
    private static final MediaType DOCX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    private final MonitorResponsivaService service;

    public MonitorResponsivaController(MonitorResponsivaService service) { this.service = service; }

    @GetMapping("/responsiva.docx")
    public ResponseEntity<byte[]> download(@PathVariable Long accessoryId, @PathVariable Long assignmentId) {
        try {
            var file = service.generate(accessoryId, assignmentId);
            return ResponseEntity.ok().contentType(DOCX).contentLength(file.content().length)
                    .cacheControl(CacheControl.noStore())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(file.filename()).build().toString())
                    .body(file.content());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }
}
