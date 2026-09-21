# Vista completa de personas

Este parche se aplica después de los cuatro parches de paridad de inventario.

Desde la raíz del proyecto:

```powershell
git status
git am --3way .\0001-feat-add-complete-person-detail-view.patch
.\mvnw.cmd -DskipTests package
git push
```

La vista queda disponible al abrir una persona desde el selector Personas de Equipos de cómputo o Teléfonos.

Si `git am` se detiene por un conflicto, no apliques el parche otra vez. Ejecuta:

```powershell
git status
```

Y comparte la salida antes de continuar.
