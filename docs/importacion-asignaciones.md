# Importación de asignaciones

La importación no crea equipos, teléfonos ni personas. Cada fila debe referenciar un registro existente y se valida completa antes de guardar.

## Equipos de cómputo

```csv
host,username,assignment_type,assigned_at,due_date,returned_at,notes
LAP-001,ana.lopez,ASSIGNMENT,2026-09-15,,,
LAP-002,luis.perez,LOAN,2026-09-15,2026-09-22,,Equipo temporal
```

## Teléfonos

```csv
imei,username,assignment_type,assigned_at,due_date,returned_at,notes
351234567890123,ana.lopez,ASSIGNMENT,2026-09-15,,,
```

Las columnas deben coincidir exactamente, aunque el orden puede cambiar. Las fechas usan `yyyy-MM-dd`; `due_date` es obligatorio únicamente para `LOAN`. Si una fila es inválida, no se importa ninguna.
