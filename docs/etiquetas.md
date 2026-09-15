# Etiquetas de equipos

En el detalle de un equipo, cada asignación activa muestra Responsiva, Etiqueta y Devolver. Responsiva es azul; Devolver es rojo. Etiqueta abre una ventana con la misma apariencia y tema del sistema.

La ventana consulta los datos actuales de esa asignación cada vez que se abre. Los cambios en los campos afectan únicamente a esa impresión. La IP editada tampoco modifica la configuración del servidor.

| Campo en la ventana | Origen |
| --- | --- |
| IP de la impresora | `asset-control.labels.printer-ip` |
| Nombre del usuario | `assignment.person.fullName` |
| Asset | `computer.asset` |
| Modelo | `computer.model` |
| Número de serie | `computer.serialNumber` |
| HOST | `computer.host` |

HOST corresponde al campo ID del programa de etiquetas anterior. El número de empleado se sigue usando en las responsivas y no sustituye al HOST en las etiquetas.

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

Si defines `allowed-printer-ips`, usa una lista de direcciones separadas por comas. Solo esas impresoras quedarán habilitadas. Sin esa lista se admiten direcciones de las redes privadas 10/8, 172.16/12 y 192.168/16. La ventana permite cambiar la IP de destino; el puerto se configura en el servidor.

## Archivos y responsabilidades

| Ruta relativa a la raíz del proyecto | Responsabilidad |
| --- | --- |
| `src/main/java/com/assetcontrol/labels/application/LabelContent.java` | Datos y límites de los campos imprimibles |
| `src/main/java/com/assetcontrol/labels/application/AssignmentLabelService.java` | Carga y validación de la asignación activa |
| `src/main/java/com/assetcontrol/labels/application/LabelPrinter.java` | Contrato de impresión |
| `src/main/java/com/assetcontrol/labels/application/LabelPrintingException.java` | Error de comunicación con la impresora |
| `src/main/java/com/assetcontrol/labels/infrastructure/ZplLabelRenderer.java` | Diseño de la etiqueta y codificación del texto |
| `src/main/java/com/assetcontrol/labels/infrastructure/TcpLabelPrinter.java` | Validación del destino y envío con tiempo límite |
| `src/main/java/com/assetcontrol/labels/web/AssignmentLabelController.java` | Endpoints de consulta e impresión |
| `src/main/resources/templates/fragments/assignment-label-dialog.html` | Ventana de edición |
| `src/main/resources/static/js/assignment-label.js` | Carga de datos, envío y mensajes |
| `src/main/resources/static/css/labels.css` | Estilos del popup en ambos temas |

En `src/main/resources/templates/computers/detail.html`, el botón Etiqueta se coloca dentro de `assignment-actions`, entre Responsiva y el formulario Devolver. Al final del documento, entre `</main>` y `</body>`, se incluye el fragmento del popup. Se reutiliza una sola ventana para todas las asignaciones.

En `src/main/resources/templates/fragments/document-head.html` se añaden los recursos `labels.css` y `assignment-label.js`. El script no realiza ninguna operación si la página no contiene la ventana de etiquetas.

## Comportamiento de impresión

GET `/computers/{computerId}/assignments/{assignmentId}/label` devuelve los datos para la ventana. POST sobre esa misma ruta valida los datos y vuelve a comprobar que la asignación siga activa antes de enviar una etiqueta.

Las asignaciones devueltas no muestran los botones de generación y sus endpoints devuelven 404. Una asignación devuelta después de abrir la ventana también se rechaza al intentar imprimir. Se conserva el comportamiento existente para Responsiva y Devolver.

La etiqueta utiliza ZPL, una copia y un área de 531 × 531 puntos, siguiendo el programa anterior. El encabezado es genérico y no incluye su gráfico incrustado. La medida física depende de la resolución de la impresora; debe revisarse con una primera etiqueta real.

El texto se envía en UTF-8 mediante `^CI28` y campos hexadecimales `^FH`, conforme a la [documentación ZPL de Zebra](https://docs.zebra.com/us/en/printers/software/zpl-pg/c-zpl-zpl-commands/r-zpl-fh.html). Se requiere una impresora compatible con ZPL y esos caracteres en su fuente. Cada dato admite hasta 100 caracteres; el diseño usa como máximo dos líneas por campo y reduce el ancho de la letra para textos largos. Si un dato del inventario excede ese límite, puedes abreviarlo en la ventana sin modificar el registro.

El mensaje «Etiqueta enviada a la impresora» confirma que se completó el envío TCP; no comprueba papel, consumibles ni la salida física. No hay reintentos automáticos. Ante un error, revisa la impresora antes de volver a imprimir para evitar copias duplicadas.

## Verificación breve

```powershell
.\mvnw.cmd "-Dtest=AssignmentLabelTests,AssignmentResponsivaTests" test
```

Las pruebas usan un receptor TCP local de prueba, sin enviar trabajos a impresoras reales. Comprueban la carga del HOST, la edición sin guardar cambios, el rechazo de asignaciones cerradas, la validación de campos y destinos y la codificación de caracteres especiales.

En el navegador, abre un equipo asignado, pulsa Etiqueta y revisa los datos. Cierra y vuelve a abrir: los datos deben volver a los valores del inventario. Comprueba ambos temas. Después, imprime una sola etiqueta desde una instalación con acceso a la impresora para validar tamaño, márgenes y acentos.
