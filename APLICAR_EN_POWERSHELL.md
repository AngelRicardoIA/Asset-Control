# Aplicar los parches

Los cuatro parches parten del commit remoto `eefdeaf`.

1. Copia los archivos de esta carpeta a la raíz de `C:\Proyectos\asset-control`.
2. Confirma que no tienes cambios pendientes con `git status`.
3. Si Git indica que existe una operación de `git am` anterior, ejecuta `git am --abort` una sola vez.
4. Ejecuta estos comandos en PowerShell:

```powershell
git pull --ff-only
git am --3way .\0001-feat-import-reference-inventory-details.patch .\0002-feat-align-phone-lifecycle-with-computers.patch .\0003-feat-add-equipment-and-people-inventory-views.patch .\0004-docs-update-inventory-import-guide.patch
$javaHome = (java -XshowSettings:properties -version 2>&1 | Select-String "java.home" | ForEach-Object { $_.Line -replace ".*=\s*", "" }).Trim()
$env:JAVA_HOME = $javaHome
.\mvnw.cmd -DskipTests package
```

5. Inicia la aplicación con `./mvnw.cmd spring-boot:run` y abre `http://localhost:8080`.

Si `git am` se detiene por un conflicto, ejecuta `git am --abort` y no continúes con el siguiente parche.
