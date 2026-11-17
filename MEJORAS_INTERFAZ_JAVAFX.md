# Mejoras de la interfaz JavaFX de TeLlevo

Se completó `vista/TelLlevoApp.java` para exponer las operaciones de los menús originales:

## Cliente
- Solicitar viajes.
- Consultar y actualizar el listado de viajes.
- Cancelar viajes.
- Calificar al conductor en viajes finalizados.
- Consultar el perfil.

## Conductor
- Cambiar disponibilidad (requiere vehículo seleccionado para ponerse disponible).
- Consultar datos y vehículos.
- Registrar y seleccionar vehículo.
- Consultar viajes disponibles y propios.
- Aceptar, iniciar y finalizar viajes.
- Calificar al pasajero en viajes finalizados.
- Consultar ganancias acumuladas.

## Administrador
- Listar y buscar usuarios.
- Registrar clientes.
- Modificar usuarios.
- Activar y desactivar usuarios.
- Asignar vehículos a conductores.
- Consultar pagos, comisión de plataforma y ganancia del conductor.

También se ajustó `VehiculoDAO.asignarAConductor` para que pueda asignar un vehículo existente al conductor indicado, respetando la regla de un vehículo por conductor, y `GestorUsuario.cargarUsuarios` para refrescar elementos ya cargados sin duplicarlos.

## Ejecución
Desde la carpeta que contiene `pom.xml`, configurar `DB_URL`, `DB_USER` y `DB_PASSWORD` en PowerShell y ejecutar:

```powershell
& "C:\Maven\apache-maven-3.10.1\bin\mvn.cmd" clean javafx:run
```

La compilación/ejecución final debe verificarse en el entorno local con la base de datos MySQL configurada.
