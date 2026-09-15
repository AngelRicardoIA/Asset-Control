package com.assetcontrol.labels.infrastructure;

import com.assetcontrol.labels.application.LabelContent;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.List;

@Component
public class ZplLabelRenderer {

    private static final int FIELD_WIDTH = 431;
    private static final int MAX_LINE_LENGTH = 50;

    public byte[] render(LabelContent content) {
        var zpl = new StringBuilder("^XA\n^CI28\n^MMT\n^PW531\n^LL531\n^LH0,0\n^LS0\n");
        zpl.append("^FO24,40^GB483,467,4^FS\n");
        zpl.append(field(48, 66, 30, 30, "Equipo de cómputo"));
        zpl.append("^FO28,110^GB475,0,3^FS\n");
        appendRow(zpl, 127, "Nombre", content.fullName());
        appendRow(zpl, 199, "ASSET", content.asset());
        appendRow(zpl, 271, "Modelo", content.model());
        appendRow(zpl, 343, "No. de serie", content.serialNumber());
        appendRow(zpl, 415, "HOST", content.host());
        zpl.append("^PQ1,0,1,Y\n^XZ\n");
        return zpl.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private void appendRow(StringBuilder zpl, int y, String title, String value) {
        var lines = lines(value);
        int longestLine = lines.stream().mapToInt(line -> line.codePointCount(0, line.length())).max().orElse(1);
        int fontWidth = Math.min(26, FIELD_WIDTH / longestLine);
        zpl.append(field(48, y, 17, 17, title));
        for (int index = 0; index < lines.size(); index++) {
            zpl.append(field(48, y + 23 + index * 24, 24, fontWidth, lines.get(index)));
        }
    }

    private List<String> lines(String value) {
        if (value == null || value.isBlank() || value.length() > 100
                || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Cada dato de la etiqueta debe tener entre 1 y 100 caracteres, sin saltos de línea.");
        }
        String text = Normalizer.normalize(value.strip(), Normalizer.Form.NFC);
        int count = text.codePointCount(0, text.length());
        if (count <= MAX_LINE_LENGTH) {
            return List.of(text);
        }
        int split = text.offsetByCodePoints(0, MAX_LINE_LENGTH);
        int space = text.lastIndexOf(' ', split);
        if (space > 0 && text.codePointCount(space + 1, text.length()) <= MAX_LINE_LENGTH) {
            split = space;
        }
        return List.of(text.substring(0, split).strip(), text.substring(split).strip());
    }

    private String field(int x, int y, int height, int width, String text) {
        String hex = HexFormat.ofDelimiter("_").withUpperCase().formatHex(text.getBytes(StandardCharsets.UTF_8));
        return "^FO" + x + "," + y + "^A0N," + height + "," + width + "^FH_^FD_" + hex + "^FS\n";
    }
}
