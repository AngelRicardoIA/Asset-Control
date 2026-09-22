# Importación de inventario

La importación de equipos crea o actualiza ubicación, persona, equipo y asignación. Cada fila se valida antes de guardar y no se crean duplicados por HOST, Asset, número de empleado o usuario.

## Equipos de cómputo

```csv
Localidad,No. Empleado,Nombre,User,Correo,Tipo,Host,Marca,Modelo,Asset,No. Serie,S.O.,Cargador,Fecha asignación
Oficina norte,10001,Ana López,ana.lopez,ana.lopez@example.test,Laptop,LAP-001,Lenovo,ThinkPad L14,AC-001,PF001,Windows 11,CHR-001,15/09/2026
```

## Teléfonos

```csv
imei,username,assignment_type,assigned_at,due_date,returned_at,notes
351234567890123,ana.lopez,ASSIGNMENT,2026-09-15,,,
```

Las columnas deben coincidir exactamente, aunque el orden puede cambiar. La fecha de asignación usa `yyyy-MM-dd` o `d/M/yyyy`. Si una fila es inválida, no se importa ninguna.
