package com.assetcontrol.labels.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LabelContent(
        @NotBlank(message = "Escribe el nombre del usuario.")
        @Size(max = 100, message = "El nombre de la etiqueta admite hasta 100 caracteres.")
        @Pattern(regexp = "[^\\p{Cntrl}]*", message = "El nombre no puede contener saltos de línea.")
        String fullName,
        @NotBlank(message = "Escribe el asset.")
        @Size(max = 100, message = "El asset admite hasta 100 caracteres.")
        @Pattern(regexp = "[^\\p{Cntrl}]*", message = "El asset no puede contener saltos de línea.")
        String asset,
        @NotBlank(message = "Escribe el modelo.")
        @Size(max = 100, message = "El modelo de la etiqueta admite hasta 100 caracteres.")
        @Pattern(regexp = "[^\\p{Cntrl}]*", message = "El modelo no puede contener saltos de línea.")
        String model,
        @NotBlank(message = "Escribe el número de serie.")
        @Size(max = 100, message = "La serie de la etiqueta admite hasta 100 caracteres.")
        @Pattern(regexp = "[^\\p{Cntrl}]*", message = "La serie no puede contener saltos de línea.")
        String serialNumber,
        @NotBlank(message = "Escribe el HOST.")
        @Size(max = 100, message = "El HOST admite hasta 100 caracteres.")
        @Pattern(regexp = "[^\\p{Cntrl}]*", message = "El HOST no puede contener saltos de línea.")
        String host
) {
}
