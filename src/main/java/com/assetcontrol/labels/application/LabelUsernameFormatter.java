package com.assetcontrol.labels.application;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class LabelUsernameFormatter {

    public String format(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("La persona asignada no tiene un nombre de usuario para la etiqueta.");
        }

        String formatted = Arrays.stream(username.strip().split("[._\\-\\s]+"))
                .filter(part -> !part.isBlank())
                .map(this::capitalize)
                .collect(Collectors.joining(" "));

        if (formatted.isBlank()) {
            throw new IllegalArgumentException("La persona asignada no tiene un nombre de usuario válido para la etiqueta.");
        }

        return formatted;
    }

    private String capitalize(String value) {
        String lowercase = value.toLowerCase(Locale.ROOT);
        return lowercase.substring(0, 1).toUpperCase(Locale.ROOT) + lowercase.substring(1);
    }
}
