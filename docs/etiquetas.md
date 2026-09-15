# Etiquetas de equipos

En el detalle de un equipo, cada asignación activa muestra Responsiva, Etiqueta y Devolver. Responsiva es azul; Devolver es rojo. Etiqueta abre una ventana con la misma apariencia y tema del sistema.

La ventana consulta los datos actuales de esa asignación cada vez que se abre. Los cambios en los campos afectan únicamente a esa impresión. La IP editada tampoco modifica la configuración del servidor.

| Campo en la ventana | Origen |
| --- | --- |
| IP de la impresora | `asset-control.labels.printer-ip` |
| Usuario mostrado en etiqueta | `assignment.person.username`, formateado para impresión |
| Asset | `computer.asset` |
| Modelo | `computer.model` |
| Número de serie | `computer.serialNumber` |
| HOST | `computer.host` |

HOST corresponde al campo ID del programa de etiquetas anterior. El número de empleado se sigue usando en las responsivas y no sustituye al HOST en las etiquetas.

El usuario se prepara solo para la etiqueta: `angel.ibanez` se muestra como `Angel Ibanez`. Los separadores `.`, `_` y `-` se convierten en espacios. El nombre completo del colaborador no se usa para este propósito.

## Configuración local

No se requieren dependencias nuevas ni migraciones. El envío sale del proceso Java por TCP, por lo que el equipo que ejecuta Asset-Control debe poder conectarse con la impresora.

Desde la carpeta del proyecto puedes definir la IP para el arranque actual:

```powershell
$env:ASSET_CONTROL_LABEL_PRINTER_IP = "IP_DE_LA_IMPRESORA"
.\mvnw.cmd spring-boot:run
```

Para conservarla entre reinicios, crea `config/application.properties` junto al `pom.xml`. Si ya existe ese archivo, añade o modifica únicamente la propiedad siguiente:

```properties
asset-control.labels.printer-ip=IP_DE_LA_IMPRESORA
```

Sustituye `IP_DE_LA_IMPRESORA` por su IPv4 real. La carpeta `config` es externa a `src`; sus archivos `.properties` quedan excluidos de Git. Esta configuración también funciona al arrancar el JAR desde la carpeta que contiene `config`.

| Variable de entorno | Propiedad | Predeterminado |
| --- | --- | --- |
| `ASSET_CONTROL_LABEL_PRINTER_IP` | `asset-control.labels.printer-ip` | Vacío; se configura por instalación |
| `ASSET_CONTROL_LABEL_PRINTER_PORT` | `asset-control.labels.printer-port` | `9100` |
| `ASSET_CONTROL_LABEL_TIMEOUT_MS` | `asset-control.labels.timeout-ms` | `5000` |
| `ASSET_CONTROL_LABEL_ALLOWED_IPS` | `asset-control.labels.allowed-printer-ips` | Vacío; permite IPv4 privadas |
| `ASSET_CONTROL_LABEL_HEADER_TEMPLATE` | `asset-control.labels.header-template` | Encabezado genérico incluido |

Si defines `allowed-printer-ips`, usa una lista de direcciones separadas por comas. Solo esas impresoras quedarán habilitadas. Sin esa lista se admiten direcciones de las redes privadas 10/8, 172.16/12 y 192.168/16. La ventana permite cambiar la IP de destino; el puerto se configura en el servidor.

## Encabezado privado por instalación

La plantilla pública conserva un encabezado genérico. Una instalación puede reemplazarlo por un gráfico propio sin agregarlo al repositorio.

1. Crea la carpeta `config/branding` junto al archivo `pom.xml`.
2. Crea dentro `label-header.zpl`.
3. Copia en ese archivo solo el comando ZPL del gráfico, empezando en `^FO` y conservando su final original. No incluyas `^XA` ni `^XZ`.
4. Añade esta propiedad a `config/application.properties`:

```properties
asset-control.labels.header-template=file:config/branding/label-header.zpl
```

La carpeta `config/branding` está excluida de Git. El código público no contiene el gráfico ni referencias a una organización concreta. Si eliminas esa propiedad, Asset-Control vuelve automáticamente al encabezado genérico.

## Archivos y responsabilidades

| Ruta relativa a la raíz del proyecto | Responsabilidad |
| --- | --- |
| `src/main/java/com/assetcontrol/labels/application/LabelContent.java` | Datos y límites de los campos imprimibles |
| `src/main/java/com/assetcontrol/labels/application/AssignmentLabelService.java` | Carga y validación de la asignación activa |
| `src/main/java/com/assetcontrol/labels/application/LabelUsernameFormatter.java` | Formato del usuario mostrado en la etiqueta |
| `src/main/java/com/assetcontrol/labels/application/LabelPrinter.java` | Contrato de impresión |
| `src/main/java/com/assetcontrol/labels/application/LabelPrintingException.java` | Error de comunicación con la impresora |
| `src/main/java/com/assetcontrol/labels/infrastructure/ZplLabelRenderer.java` | Diseño de la etiqueta y codificación del texto |
| `src/main/java/com/assetcontrol/labels/infrastructure/TcpLabelPrinter.java` | Validación del destino y envío con tiempo límite |
| `src/main/java/com/assetcontrol/labels/web/AssignmentLabelController.java` | Endpoints de consulta e impresión |
| `src/main/resources/templates/fragments/assignment-label-dialog.html` | Ventana de edición |
| `src/main/resources/static/js/assignment-label.js` | Carga de datos, envío y mensajes |
| `src/main/resources/static/css/labels.css` | Estilos del popup en ambos temas |
| `src/main/resources/label-templates/legacy-layout.zpl` | Medidas y posiciones heredadas de la etiqueta anterior |
| `src/main/resources/label-templates/generic-header.zpl` | Encabezado público predeterminado |

En `src/main/resources/templates/computers/detail.html`, el botón Etiqueta se coloca dentro de `assignment-actions`, entre Responsiva y el formulario Devolver. Al final del documento, entre `</main>` y `</body>`, se incluye el fragmento del popup. Se reutiliza una sola ventana para todas las asignaciones.

En `src/main/resources/templates/fragments/document-head.html` se añaden los recursos `labels.css` y `assignment-label.js`. El script no realiza ninguna operación si la página no contiene la ventana de etiquetas.

## Comportamiento de impresión

GET `/computers/{computerId}/assignments/{assignmentId}/label` devuelve los datos para la ventana. POST sobre esa misma ruta valida los datos y vuelve a comprobar que la asignación siga activa antes de enviar una etiqueta.

Las asignaciones devueltas no muestran los botones de generación y sus endpoints devuelven 404. Una asignación devuelta después de abrir la ventana también se rechaza al intentar imprimir. Se conserva el comportamiento existente para Responsiva y Devolver.

La etiqueta utiliza ZPL, una copia y un área de 531 × 531 puntos. Conserva el marco de 489 × 418, el separador horizontal, las posiciones y los tamaños de fuente de la etiqueta anterior. No ajusta texto ni crea saltos de línea; si un valor no cabe físicamente, se puede abreviar en la ventana antes de imprimir. La medida física depende de la resolución de la impresora; debe revisarse con una primera etiqueta real.

El texto se envía en UTF-8 mediante `^CI28` y campos hexadecimales `^FH`, conforme a la [documentación ZPL de Zebra](https://docs.zebra.com/us/en/printers/software/zpl-pg/c-zpl-zpl-commands/r-zpl-fh.html). Se requiere una impresora compatible con ZPL y esos caracteres en su fuente. Cada dato admite hasta 100 caracteres, pero el diseño no reduce la fuente para valores largos. Si un dato del inventario no cabe, abrevia únicamente el valor de la ventana sin modificar el registro.

El mensaje «Etiqueta enviada a la impresora» confirma que se completó el envío TCP; no comprueba papel, consumibles ni la salida física. No hay reintentos automáticos. Ante un error, revisa la impresora antes de volver a imprimir para evitar copias duplicadas.

## Verificación breve

```powershell
.\mvnw.cmd "-Dtest=AssignmentLabelTests,AssignmentResponsivaTests" test
```

Las pruebas usan un receptor TCP local de prueba, sin enviar trabajos a impresoras reales. Comprueban la carga del HOST, la edición sin guardar cambios, el rechazo de asignaciones cerradas, la validación de campos y destinos y la codificación de caracteres especiales.

En el navegador, abre un equipo asignado, pulsa Etiqueta y revisa los datos. Cierra y vuelve a abrir: los datos deben volver a los valores del inventario. Comprueba ambos temas. Después, imprime una sola etiqueta desde una instalación con acceso a la impresora para validar tamaño, márgenes y acentos.
