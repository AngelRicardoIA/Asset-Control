# Integrar responsivas en Asset-Control

La implementación se preparó sobre el commit público `9fe00c709fdfb1440a37f08f47bc70dc27642128` del repositorio `AngelRicardoIA/Asset-Control`.

El flujo queda así: guardar una asignación, abrir el detalle del equipo en esa asignación y pulsar **Generar y descargar responsiva**. También se abre el detalle al registrar un equipo con asignación inicial. El botón queda disponible en el historial para descargar nuevamente.

Se reutilizan los estilos y los fragmentos HTML existentes. Los datos se leen de la asignación guardada; descargar el documento no registra movimientos ni cambia estados. Esta versión genera el archivo con los datos actuales registrados. No conserva una copia histórica del documento ni la versión firmada; ese almacenamiento y SharePoint quedan para el siguiente bloque.

## 1. Colocar tu plantilla

Crea la carpeta `document-templates` dentro de `C:\Proyectos\asset-control\src\main\resources` y copia allí tu archivo. Su ruta completa debe quedar:

```text
C:\Proyectos\asset-control\src\main\resources\document-templates\responsiva-asignacion-placeholder.docx
```

Debe ser un documento DOCX real. Los marcadores que pegaste ya corresponden a la implementación. El archivo no se obtuvo en esta sesión; las verificaciones del generador utilizan documentos de prueba creados en memoria.

| Marcador | Origen en tu código |
| --- | --- |
| `{{employee.fullName}}` | `assignment.getPerson().getFullName()` |
| `{{employee.number}}` | `assignment.getPerson().getExternalId()` |
| `{{computer.model}}` | `assignment.getComputer().getModel()` |
| `{{computer.asset}}` | `assignment.getComputer().getAsset()` |
| `{{computer.serialNumber}}` | `assignment.getComputer().getSerialNumber()` |
| `{{assignment.assignedAt}}` | `assignment.getAssignedAt()`, con formato `dd/MM/yyyy` |

`externalId` es el número de empleado que registras en el formulario. Se mantiene como texto para conservar ceros iniciales.

Los marcadores pueden repetirse y tener formato. Se procesan párrafos, tablas, encabezados y pies de página, incluso si Word dividió un marcador entre varios segmentos de texto. Mantén los marcadores como texto normal dentro de un mismo párrafo; esta versión no contempla cuadros de texto, controles de contenido o campos de combinación de correspondencia.

## 2. Aplicar los cambios con Git

El ZIP contiene `responsivas.patch`, esta guía y `nuevos`, con los archivos nuevos completos para revisarlos. Copia `responsivas.patch` a `C:\Proyectos\asset-control` y abre PowerShell en esa carpeta.

Ejecuta primero:

```powershell
git status --short
git apply --check .\responsivas.patch
```

Si el segundo comando termina sin errores, aplica el cambio:

```powershell
git apply .\responsivas.patch
git diff --stat
git status --short
```

El parche agrega los archivos nuevos y modifica únicamente los puntos descritos en el apartado 3. No necesitas copiarlos también desde `nuevos`. La plantilla que creaste se agrega por separado en el paso 1.

Si Git informa que un bloque no coincide, el chequeo no habrá modificado tus archivos. Puedes usar los cambios manuales exactos del apartado 3 para conservar las modificaciones locales que no están en el repositorio público. No fuerces la aplicación del parche ni reemplaces controladores completos con versiones antiguas.

## 3. Cambios manuales exactos

Este apartado es la alternativa a aplicar el parche. Si ya lo aplicaste correctamente, pasa al apartado 4.

Copia el contenido de `nuevos\src` dentro de `C:\Proyectos\asset-control\src`, conservando la estructura. Se agregan estos archivos:

```text
src/main/java/com/assetcontrol/documents/application/DocxTemplateRenderer.java
src/main/java/com/assetcontrol/documents/application/ResponsivaGenerationException.java
src/main/java/com/assetcontrol/documents/application/GeneratedResponsiva.java
src/main/java/com/assetcontrol/documents/application/AssignmentResponsivaService.java
src/main/java/com/assetcontrol/documents/infrastructure/PoiDocxTemplateRenderer.java
src/main/java/com/assetcontrol/documents/web/AssignmentResponsivaController.java
src/main/resources/templates/assignments/responsiva-error.html
src/test/java/com/assetcontrol/documents/AssignmentResponsivaTests.java
```

### A. Dependencia

En `C:\Proyectos\asset-control\pom.xml`, dentro de `<dependencies>`, agrega:

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.5.1</version>
</dependency>
```

Se usa [Apache POI XWPF](https://poi.apache.org/components/document/index.html) para leer y escribir DOCX. La [distribución de POI 5.5.1](https://poi.apache.org/download.html) está disponible en Maven Central. La generación se ejecuta dentro de Java; el servidor no necesita iniciar Word.

### B. Ruta de la plantilla

Al final de `C:\Proyectos\asset-control\src\main\resources\application.properties`, agrega:

```properties
asset-control.documents.assignment-template=${ASSET_CONTROL_ASSIGNMENT_TEMPLATE:classpath:document-templates/responsiva-asignacion-placeholder.docx}
```

La ruta predeterminada funciona dentro del JAR. Para utilizar una plantilla externa en otra instalación, puedes configurar la variable de entorno antes de arrancar, por ejemplo:

```powershell
$env:ASSET_CONTROL_ASSIGNMENT_TEMPLATE = 'file:C:/asset-control/documentos/responsiva-asignacion-placeholder.docx'
```

Esta variable es opcional; para la presentación basta con la ruta del paso 1.

### C. Volver a la asignación recién guardada

Abre `C:\Proyectos\asset-control\src\main\java\com\assetcontrol\assignments\web\ComputerAssignmentController.java`.

Dentro del método `createAssignment`, localiza el inicio de `try` con estas dos líneas:

```java
assignmentService.create(assignmentForm.toCommand(computerId));
return "redirect:/computers/" + computerId;
```

Reemplaza únicamente esas dos líneas por:

```java
var savedAssignment = assignmentService.create(assignmentForm.toCommand(computerId));
return "redirect:/computers/" + computerId + "#asignacion-" + savedAssignment.getId();
```

El resto del método, incluidas sus validaciones y sus bloques `catch`, se conserva.

### D. Asignación inicial de un equipo nuevo

Abre `C:\Proyectos\asset-control\src\main\java\com\assetcontrol\computers\web\ComputerController.java`.

Dentro de `createComputer`, localiza este bloque dentro del `try`:

```java
registrationService.register(new RegisterComputerCommand(
        computerForm.toCommand(),
        computerForm.getInitialAssignment().hasData()
                ? computerForm.getInitialAssignment().toCommand(null)
                : null
));

return "redirect:/computers";
```

Reemplaza ese bloque por:

```java
Computer savedComputer = registrationService.register(new RegisterComputerCommand(
        computerForm.toCommand(),
        computerForm.getInitialAssignment().hasData()
                ? computerForm.getInitialAssignment().toCommand(null)
                : null
));

return "redirect:/computers/" + savedComputer.getId() + "#asignaciones";
```

`Computer` ya está importado en la versión revisada. El resto del controlador se conserva.

### E. Botón en el historial del equipo

Abre `C:\Proyectos\asset-control\src\main\resources\templates\computers\detail.html`.

Busca:

```html
<section class="form-panel assignment-history">
```

Y cambia esa etiqueta por:

```html
<section id="asignaciones" class="form-panel assignment-history">
```

Dentro de esa sección, busca:

```html
<article class="assignment-item" th:each="assignment : ${assignments}">
```

Y reemplaza la etiqueta de apertura por:

```html
<article class="assignment-item" th:each="assignment : ${assignments}"
         th:id="${'asignacion-' + assignment.id}">
```

Dentro de ese `article`, en el `div` con clase `assignment-status`, coloca el siguiente enlace inmediatamente antes del `<form>` que contiene el botón **Devolver**, y fuera de ese formulario:

```html
<a
        class="primary-button"
        th:href="@{/computers/{computerId}/assignments/{assignmentId}/responsiva.docx(computerId=${computer.id}, assignmentId=${assignment.id})}"
>Generar y descargar responsiva</a>
```

El enlace queda asociado al identificador de cada asignación. Esto permite que un equipo asignado a varias personas tenga una responsiva para cada una.

## 4. Compilar y comprobar

Recarga Maven en IntelliJ tras agregar la dependencia. En PowerShell, desde `C:\Proyectos\asset-control`, ejecuta:

```powershell
.\mvnw.cmd -DskipTests package
.\mvnw.cmd spring-boot:run
```

No hay migraciones nuevas ni es necesario recrear la base de datos.

Abre un equipo, guarda una asignación y pulsa **Generar y descargar responsiva**. Confirma en Word que aparecen los seis datos y que la fecha corresponde a la asignación. Pulsa otra vez el botón: debe descargar otro archivo sin crear otro movimiento. El nombre será similar a `responsiva-HOST-DEMO-asignacion-41.docx`.

Las pruebas automatizadas de este bloque se ejecutan juntas con:

```powershell
.\mvnw.cmd -Dtest=AssignmentResponsivaTests test
```

Verificación de esta entrega: compilación correcta con Java 21 y Spring Boot 4.1.1; 6 pruebas ejecutadas, 0 fallos y 0 errores.

Cubren marcadores divididos, repetidos y en tablas, encabezados y pies; preservación del formato de segmentos; número de empleado con ceros iniciales; errores de plantilla; cabeceras HTTP de descarga; y rechazo de una asignación que pertenezca a otro equipo. Estas pruebas usan datos sintéticos y no abren tu base de datos.

Si la descarga muestra un error de plantilla, revisa primero la ruta y los marcadores. El error no borra ni crea asignaciones; puedes corregir la plantilla y descargar de nuevo.

## 5. Commit

Revisa `git status --short`. Agrega los cambios de este bloque y la plantilla genérica. Puedes hacerlo con:

```powershell
git add pom.xml src/main/resources/application.properties
git add src/main/java/com/assetcontrol/documents src/test/java/com/assetcontrol/documents
git add src/main/java/com/assetcontrol/assignments/web/ComputerAssignmentController.java
git add src/main/java/com/assetcontrol/computers/web/ComputerController.java
git add src/main/resources/templates/computers/detail.html
git add src/main/resources/templates/assignments/responsiva-error.html
git add src/main/resources/document-templates/responsiva-asignacion-placeholder.docx
git diff --cached --stat
git commit -m "feat: generate assignment responsivas in docx"
```

`responsivas.patch` es el archivo de instalación y no forma parte del commit. Los documentos generados con datos reales tampoco forman parte de este cambio.
