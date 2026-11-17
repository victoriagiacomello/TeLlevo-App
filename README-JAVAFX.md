# TeLlevo - Interfaz JavaFX

## Requisitos
- JDK 21
- Maven
- MySQL configurado con la base de datos que ya utiliza el proyecto

## Ejecutar
Desde la carpeta donde está este `pom.xml`, ejecutar:

```bash
mvn clean javafx:run
```

## Cambios incorporados
- JavaFX Controls 21.0.2 y plugin Maven de JavaFX.
- `App.java` inicia la aplicación gráfica.
- Nueva clase `vista.TelLlevoApp` con login, registro y paneles por tipo de usuario.
- Hoja de estilos `src/main/resources/css/estilos.css`.
- Conserva los paquetes de modelos, gestores, repositorios, autenticación y menús existentes.

La interfaz reutiliza la lógica de negocio y la persistencia existentes. No se reemplazaron los DAO ni los gestores.
