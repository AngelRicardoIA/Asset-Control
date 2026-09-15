package com.assetcontrol.labels.infrastructure;

import com.assetcontrol.labels.application.LabelContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ZplLabelRenderer {

    private static final String HEADER = "{HEADER}";
    private static final String NAME = "{NAME}";
    private static final String ASSET = "{ASSET}";
    private static final String MODEL = "{MODEL}";
    private static final String SERIAL_NUMBER = "{SERIAL_NUMBER}";
    private static final String HOST = "{HOST}";

    private final String layout;
    private final String header;

    public ZplLabelRenderer(
            @Value("${asset-control.labels.layout-template:classpath:label-templates/legacy-layout.zpl}") Resource layoutTemplate,
            @Value("${asset-control.labels.header-template:classpath:label-templates/generic-header.zpl}") Resource headerTemplate
    ) {
        this.layout = read(layoutTemplate, "la plantilla de la etiqueta");
        this.header = read(headerTemplate, "el encabezado de la etiqueta");
        validateLayout();
        validateHeader();
    }

    public byte[] render(LabelContent content) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(HEADER, header);
        values.put(NAME, encode(content.displayName()));
        values.put(ASSET, encode(content.asset()));
        values.put(MODEL, encode(content.model()));
        values.put(SERIAL_NUMBER, encode(content.serialNumber()));
        values.put(HOST, encode(content.host()));

        String zpl = layout;
        for (Map.Entry<String, String> value : values.entrySet()) {
            zpl = zpl.replace(value.getKey(), value.getValue());
        }
        return zpl.getBytes(StandardCharsets.US_ASCII);
    }

    private String read(Resource resource, String label) {
        try (var input = resource.getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo cargar " + label + ".", exception);
        }
    }

    private void validateLayout() {
        for (String token : new String[] {HEADER, NAME, ASSET, MODEL, SERIAL_NUMBER, HOST}) {
            if (occurrences(layout, token) != 1) {
                throw new IllegalStateException("La plantilla de la etiqueta debe incluir una sola vez " + token + ".");
            }
        }
        if (occurrences(layout, "^XA") != 1 || occurrences(layout, "^XZ") != 1) {
            throw new IllegalStateException("La plantilla de la etiqueta debe contener un solo formato ZPL completo.");
        }
    }

    private void validateHeader() {
        if (header.isBlank()) {
            throw new IllegalStateException("El encabezado de la etiqueta no puede estar vacío.");
        }
        if (header.contains("^XA") || header.contains("^XZ")) {
            throw new IllegalStateException("El encabezado no debe incluir ^XA ni ^XZ.");
        }
    }

    private int occurrences(String value, String fragment) {
        int occurrences = 0;
        int index = 0;
        while ((index = value.indexOf(fragment, index)) >= 0) {
            occurrences++;
            index += fragment.length();
        }
        return occurrences;
    }

    private String encode(String value) {
        if (value == null || value.isBlank() || value.length() > 100
                || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Cada dato de la etiqueta debe tener entre 1 y 100 caracteres, sin saltos de línea.");
        }
        String text = Normalizer.normalize(value.strip(), Normalizer.Form.NFC);
        return "_" + HexFormat.ofDelimiter("_").withUpperCase().formatHex(text.getBytes(StandardCharsets.UTF_8));
    }
}
