package com.assetcontrol.documents.infrastructure;

import com.assetcontrol.documents.application.DocxTemplateRenderer;
import com.assetcontrol.documents.application.ResponsivaGenerationException;
import org.apache.poi.xwpf.usermodel.IBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Component
public class PoiDocxTemplateRenderer implements DocxTemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^{}]+)}}");

    private final Resource template;

    public PoiDocxTemplateRenderer(
            @Value("${asset-control.documents.assignment-template}") Resource template
    ) {
        this.template = template;
    }

    @Override
    public byte[] render(Map<String, String> values) {
        try (var input = template.getInputStream();
             var document = new XWPFDocument(input);
             var output = new ByteArrayOutputStream()) {
            Set<String> found = new HashSet<>();
            replaceBody(document, values, found);
            for (var header : document.getHeaderList()) {
                replaceBody(header, values, found);
            }
            for (var footer : document.getFooterList()) {
                replaceBody(footer, values, found);
            }

            Set<String> missing = new LinkedHashSet<>(values.keySet());
            missing.removeAll(found);
            if (!missing.isEmpty()) {
                throw new ResponsivaGenerationException(
                        "Faltan marcadores en la plantilla: " + String.join(", ", missing) + "."
                );
            }

            document.write(output);
            return output.toByteArray();
        } catch (ResponsivaGenerationException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new ResponsivaGenerationException(
                    "No se pudo leer o generar la responsiva. Revisa la ruta y el formato DOCX de la plantilla.",
                    exception
            );
        }
    }

    private void replaceBody(IBody body, Map<String, String> values, Set<String> found) {
        for (var paragraph : body.getParagraphs()) {
            replaceParagraph(paragraph, values, found);
        }
        for (var table : body.getTables()) {
            for (var row : table.getRows()) {
                for (var cell : row.getTableCells()) {
                    replaceBody(cell, values, found);
                }
            }
        }
    }

    private void replaceParagraph(
            XWPFParagraph paragraph,
            Map<String, String> values,
            Set<String> found
    ) {
        StringBuilder text = new StringBuilder();
        List<TextPart> parts = new ArrayList<>();
        for (var run : paragraph.getRuns()) {
            for (int index = 0; index < run.getCTR().sizeOfTArray(); index++) {
                String value = run.getText(index);
                int start = text.length();
                text.append(value);
                parts.add(new TextPart(run, index, start, text.length()));
            }
        }

        List<MatchResult> matches = PLACEHOLDER.matcher(text).results().toList();
        for (int index = matches.size() - 1; index >= 0; index--) {
            MatchResult match = matches.get(index);
            String key = match.group(1).trim();
            String replacement = values.get(key);
            if (replacement == null) {
                throw new ResponsivaGenerationException(
                        "La plantilla contiene un marcador no configurado: " + match.group() + "."
                );
            }
            found.add(key);
            replaceMatch(parts, match, replacement);
        }
    }

    private void replaceMatch(List<TextPart> parts, MatchResult match, String replacement) {
        for (int index = parts.size() - 1; index >= 0; index--) {
            TextPart part = parts.get(index);
            if (part.end() <= match.start() || part.start() >= match.end()) {
                continue;
            }

            String current = part.run().getText(part.textIndex());
            int start = Math.max(match.start() - part.start(), 0);
            int end = Math.min(match.end(), part.end()) - part.start();
            String inserted = part.start() <= match.start() ? replacement : "";
            part.run().setText(
                    current.substring(0, start) + inserted + current.substring(end),
                    part.textIndex()
            );
        }
    }

    private record TextPart(XWPFRun run, int textIndex, int start, int end) {
    }
}
