# Aplicar colores y etiquetas

Estos cambios parten del commit `7ee24fe` del repositorio. Incluyen dos bloques independientes para que puedas revisarlos y hacer un commit después de cada uno.

El archivo de entrega contiene `01-colores.patch`, `02-etiquetas.patch` y una carpeta `archivos` con las versiones completas de todos los archivos añadidos o modificados. Los parches realizan las modificaciones exactas; no es necesario copiar fragmentos de código a mano. La carpeta `archivos` sirve para leer y comparar los archivos completos.

## 1. Preparar la carpeta

Extrae la entrega en `C:\Proyectos\asset-control-cambios`, fuera del proyecto. Los dos archivos `.patch` deben quedar directamente dentro de esa carpeta.

Detén la aplicación con `Ctrl+C`. Abre PowerShell en el proyecto:

```powershell
Set-Location C:\Proyectos\asset-control
git status --short
```

Parte de un directorio de trabajo limpio. Si aparecen cambios propios, guárdalos mediante tu commit habitual antes de aplicar los parches. No se necesita borrar archivos ni reiniciar el repositorio.

```powershell
$cambios = "C:\Proyectos\asset-control-cambios"
```

## 2. Colores y primer commit

```powershell
git apply --check --index "$cambios\01-colores.patch"
```

Si termina sin errores, aplica el cambio:

```powershell
git apply --index "$cambios\01-colores.patch"
git diff --cached --stat
git commit -m "fix: make responsiva blue and return action red"
```

Este bloque modifica `src/main/resources/static/css/tokens.css`, `src/main/resources/static/css/forms.css` y `src/main/resources/templates/computers/detail.html`. Añade el color azul reutilizable, conserva el botón Devolver rojo y mantiene las acciones alineadas con tamaños compactos.

## 3. Etiquetas y segundo commit

```powershell
git apply --check --index "$cambios\02-etiquetas.patch"
```

Si termina sin errores:

```powershell
git apply --index "$cambios\02-etiquetas.patch"
.\mvnw.cmd "-Dtest=AssignmentLabelTests,AssignmentResponsivaTests" test
```

Cuando la compilación y esas pruebas terminen correctamente:

```powershell
git diff --cached --stat
git commit -m "feat: print assignment labels from an editable dialog"
```

`git apply --index` deja preparados para el commit únicamente los archivos del parche. Los parches y el ZIP permanecen fuera del proyecto.

Si una comprobación muestra `patch does not apply` u otro error, no uses `--reject` ni reemplaces archivos por fuerza. El parche no coincide con tu copia actual; conserva la salida del error para adaptar ese cambio a tus archivos.

## 4. Configurar tu impresora

Crea la carpeta `C:\Proyectos\asset-control\config` y dentro un archivo `application.properties`. Si ese archivo ya existe, conserva su contenido y añade o modifica esta propiedad:

```properties
asset-control.labels.printer-ip=IP_DE_LA_IMPRESORA
```

Sustituye el valor por la IP de tu impresora. Este archivo es configuración de tu instalación y queda excluido de Git. El puerto predeterminado es 9100 y el tiempo límite de envío es de cinco segundos.

Arranca desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

En el navegador, entra al detalle de un equipo con una asignación activa. Verás Responsiva, Etiqueta y Devolver. Pulsa Etiqueta para abrir la ventana y revisar los datos antes de imprimir.

No hacen falta cambios en `pom.xml`, instalaciones de Python ni nuevas migraciones de la base de datos. El servidor Java envía el trabajo a la impresora; debe tener acceso de red a ella.

## 5. Qué comprobar

1. Responsiva azul y Devolver rojo, sobre la misma línea en escritorio.
2. Etiqueta abre la ventana con nombre, asset, modelo, serie, HOST e IP.
3. Al cerrar y volver a abrir, se recargan los valores de la base de datos y la IP configurada.
4. Cambiar datos en la ventana no modifica el equipo ni a la persona.
5. Las asignaciones devueltas no muestran los botones de generación.
6. Una primera impresión real confirma el tamaño, los márgenes y los caracteres acentuados de tu impresora.

Las pruebas automatizadas usan un receptor TCP local de prueba. La verificación física debe hacerse en la red donde está instalada la impresora.

Consulta `docs/etiquetas.md` para el mapa de campos, los endpoints, las opciones de configuración y las responsabilidades de cada archivo.
