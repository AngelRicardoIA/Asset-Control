# Importación de inventario y asignaciones

Cada archivo se valida completo antes de modificar el inventario. Si una columna o fila es inválida, no se importa ningún registro.

## Inventario de equipos de cómputo

Este formato crea o actualiza ubicaciones, personas y equipos, y registra la asignación activa indicada en cada fila.

```csv
Localidad,No. Empleado,Nombre,Puesto,Area,Gerente,User,Correo,Tipo,Host,Marca,Modelo,Asset,No. Serie,S.O.,Cargador,Fecha asignación
Oficina Norte,1042,Ana López,Analista,Operaciones,Laura Gómez,ana.lopez,ana.lopez@example.com,Laptop,LAP-001,Lenovo,ThinkPad L14,AC-001,PF123456,Windows 11,CH-001,2026-09-15
```

Las columnas deben llamarse exactamente como en el encabezado, aunque el orden puede cambiar. `Tipo` acepta `Laptop`, `PC`, `Desktop` o `Equipo de escritorio`. `Fecha asignación` acepta `yyyy-MM-dd`, `d/M/yyyy` o `d-M-yyyy`.

Si un HOST o Asset ya existe, ambos deben identificar el mismo equipo. Si un No. Empleado o User ya existe, ambos deben identificar a la misma persona. La importación no elimina asignaciones existentes.

## Asignaciones históricas de teléfonos

Este formato requiere que el teléfono y la persona ya existan.

```csv
imei,username,assignment_type,assigned_at,due_date,returned_at,notes
351234567890123,ana.lopez,ASSIGNMENT,2026-09-15,,,
351234567890124,luis.perez,LOAN,2026-09-15,2026-09-22,,Equipo temporal
```

Las columnas deben coincidir exactamente, aunque el orden puede cambiar. Las fechas usan `yyyy-MM-dd`; `due_date` es obligatorio únicamente para `LOAN`. Si una fila es inválida, no se importa ninguna.
