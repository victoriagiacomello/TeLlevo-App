package vista;

import autenticacion.Autenticacion;
import enums.EstadoPago;
import enums.EstadoViaje;
import enums.MetodoPago;
import gestores.GestorPagos;
import gestores.GestorCalificaciones;
import gestores.GestorUsuario;
import gestores.GestorViajes;
import modelo.*;
import repositorio.UsuarioDAO;
import repositorio.VehiculoDAO;
import enums.TipoUsuario;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class TelLlevoApp {
    private final Stage stage;
    private final Autenticacion autenticacion = new Autenticacion();
    private final GestorViajes gestorViajes = new GestorViajes();
    private final GestorUsuario gestorUsuario = new GestorUsuario();
    private final GestorPagos gestorPagos = new GestorPagos();
    private final GestorCalificaciones gestorCalificaciones = new GestorCalificaciones();
    private Usuario usuarioActual;

    private static final String VERDE = "#263B2E";
    private static final String OLIVA = "#687653";
    private static final String SALVIA = "#A5AD91";
    private static final String MARFIL = "#F3F1E8";
    private static final String TEXTO = "#263329";

    public TelLlevoApp(Stage stage) {
        this.stage = stage;
        try {
            gestorViajes.cargarViajes();
        } catch (SQLException e) {
            alerta("Base de datos", "No se pudieron cargar los viajes: " + e.getMessage());
        }
        stage.setTitle("TeLlevo | Movilidad");
        stage.setMinWidth(980);
        stage.setMinHeight(650);
    }

    public void mostrarLogin() {
        VBox brand = new VBox(18);
        brand.setPadding(new Insets(55, 48, 45, 48));
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPrefWidth(420);
        brand.setStyle("-fx-background-color:" + VERDE + ";");
        Label logo = new Label("TeLlevo");
        logo.setStyle("-fx-text-fill:#F3F1E8;-fx-font-size:38px;-fx-font-weight:bold;");
        Label slogan = new Label("Movete con estilo.");
        slogan.setStyle("-fx-text-fill:#F3F1E8;-fx-font-size:25px;-fx-font-weight:600;");
        Label desc = new Label("Conectamos personas\ncon nuevos destinos.");
        desc.setStyle("-fx-text-fill:#D9DFD1;-fx-font-size:15px;");
        brand.getChildren().addAll(logo, slogan, desc);

        TextField email = new TextField();
        email.setPromptText("Correo electrónico");
        PasswordField password = new PasswordField();
        password.setPromptText("Contraseña");

        VBox form = new VBox(15);
        form.setMaxWidth(340);
        form.setAlignment(Pos.CENTER_LEFT);
        Label title = heading("Bienvenido/a");
        Label subtitle = muted("Ingresá a tu cuenta para continuar");
        Button ingresar = primaryButton("Iniciar sesión");
        Button registrar = secondaryButton("Crear una cuenta");
        Label error = muted("");
        ingresar.setMaxWidth(Double.MAX_VALUE);
        registrar.setMaxWidth(Double.MAX_VALUE);
        ingresar.setOnAction(e -> {
            try {
                Usuario u = autenticacion.iniciarSesion(email.getText().trim(), password.getText());
                if (u == null) {
                    error.setText("El correo o la contraseña son incorrectos.");
                    return;
                }
                usuarioActual = u;
                mostrarPanelUsuario(u);
            } catch (Exception ex) {
                error.setText(ex.getMessage());
            }
        });
        password.setOnAction(e -> ingresar.fire());
        registrar.setOnAction(e -> mostrarRegistro());
        form.getChildren().addAll(title, subtitle, new Label("Correo electrónico"), email,
                new Label("Contraseña"), password, ingresar, registrar, error);

        StackPane right = new StackPane(form);
        right.setPadding(new Insets(45));
        right.setStyle("-fx-background-color:" + MARFIL + ";");
        HBox root = new HBox(brand, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        Scene scene = new Scene(root, 1050, 700);
        scene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarRegistro() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear cuenta | TeLlevo");
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Registrarme", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Cliente", "Conductor"));
        tipo.getSelectionModel().selectFirst();
        TextField nombre = field("Nombre");
        TextField apellido = field("Apellido");
        TextField dni = field("DNI");
        TextField email = field("Correo electrónico");
        TextField telefono = field("Teléfono");
        PasswordField pass = new PasswordField(); pass.setPromptText("Contraseña (mínimo 8 caracteres)");
        TextField licencia = field("Número de licencia (solo conductor)");
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(15));
        int r=0;
        grid.add(new Label("Tipo de cuenta"),0,r); grid.add(tipo,1,r++);
        grid.add(new Label("Nombre"),0,r); grid.add(nombre,1,r++);
        grid.add(new Label("Apellido"),0,r); grid.add(apellido,1,r++);
        grid.add(new Label("DNI"),0,r); grid.add(dni,1,r++);
        grid.add(new Label("Email"),0,r); grid.add(email,1,r++);
        grid.add(new Label("Teléfono"),0,r); grid.add(telefono,1,r++);
        grid.add(new Label("Contraseña"),0,r); grid.add(pass,1,r++);
        grid.add(new Label("Licencia"),0,r); grid.add(licencia,1,r++);
        licencia.disableProperty().bind(tipo.valueProperty().isEqualTo("Cliente"));
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait().ifPresent(result -> {
            if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    Usuario nuevo = tipo.getValue().equals("Conductor")
                            ? new Conductor(nombre.getText(), apellido.getText(), dni.getText(),
                                email.getText(), telefono.getText(), pass.getText(), licencia.getText())
                            : new Cliente(nombre.getText(), apellido.getText(), dni.getText(),
                                email.getText(), telefono.getText(), pass.getText());
                    gestorUsuario.registrarUsuario(nuevo);
                    alerta("Cuenta creada", "Tu cuenta fue registrada correctamente. Ya podés iniciar sesión.");
                    mostrarLogin();
                } catch (Exception ex) {
                    alerta("No se pudo registrar", ex.getMessage());
                    mostrarRegistro();
                }
            }
        });
    }

    private void mostrarPanelUsuario(Usuario usuario) {
        if (usuario instanceof Cliente c) mostrarCliente(c);
        else if (usuario instanceof Conductor c) mostrarConductor(c);
        else mostrarAdministrador();
    }

    private BorderPane basePanel(String seccion) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + MARFIL + ";");
        HBox top = new HBox(15);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(18, 28, 18, 28));
        top.setStyle("-fx-background-color:" + VERDE + ";");
        Label logo = new Label("TeLlevo");
        logo.setStyle("-fx-text-fill:#F3F1E8;-fx-font-size:25px;-fx-font-weight:bold;");
        Label page = new Label(seccion);
        page.setStyle("-fx-text-fill:#D9DFD1;-fx-font-size:14px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button salir = new Button("Cerrar sesión");
        salir.setOnAction(e -> { if (usuarioActual != null) autenticacion.cerrarSesion(usuarioActual); usuarioActual=null; mostrarLogin(); });
        top.getChildren().addAll(logo, page, spacer, salir);
        root.setTop(top);
        return root;
    }

    private void mostrarCliente(Cliente cliente) {
        BorderPane root = basePanel("Área de cliente");
        VBox content = content();
        content.getChildren().addAll(heading("Hola, " + cliente.getNombre()), muted("¿A dónde querés ir hoy?"));
        HBox cards = new HBox(14,
                metricCard("Tus viajes", String.valueOf(gestorViajes.listarPorClientes(cliente).size())),
                metricCard("Tu cuenta", "Activa"));
        content.getChildren().add(cards);

        TextField origen = field("Punto de partida");
        TextField destino = field("Destino");
        ComboBox<MetodoPago> pago = new ComboBox<>(FXCollections.observableArrayList(MetodoPago.values()));
        pago.setPromptText("Método de pago");
        Button solicitar = primaryButton("Solicitar viaje");
        Label estado = muted("");
        solicitar.setOnAction(e -> {
            try {
                Viaje v = gestorViajes.solicitarViaje(cliente, origen.getText(), destino.getText(), pago.getValue());
                estado.setText("¡Viaje solicitado! Número de viaje: " + v.getIdViaje());
                origen.clear(); destino.clear(); pago.setValue(null);
                refrescarTablaCliente(tablaViajes(cliente), cliente);
            } catch (Exception ex) { alerta("No se pudo solicitar el viaje", ex.getMessage()); }
        });
        GridPane request = new GridPane(); request.setHgap(10); request.setVgap(10);
        request.add(new Label("Origen"),0,0); request.add(origen,1,0);
        request.add(new Label("Destino"),0,1); request.add(destino,1,1);
        request.add(new Label("Pago"),0,2); request.add(pago,1,2);
        request.add(solicitar,1,3);
        content.getChildren().addAll(sectionTitle("Solicitar un viaje"), request, estado,
                sectionTitle("Mis viajes"));
        TableView<Viaje> table = tablaViajes(cliente);
        content.getChildren().add(table);
        Button cancelar = secondaryButton("Cancelar viaje seleccionado");
        cancelar.setOnAction(e -> {
            Viaje v=table.getSelectionModel().getSelectedItem();
            if(v==null){ alerta("Seleccioná un viaje","Elegí un viaje de la tabla."); return; }
            try { gestorViajes.cancelarViaje(v.getIdViaje()); refrescarTablaCliente(table,cliente); }
            catch(Exception ex){ alerta("No se pudo cancelar",ex.getMessage()); }
        });
        Button calificar = secondaryButton("Calificar conductor");
        calificar.setOnAction(e -> dialogoCalificar(cliente, table));
        Button perfil = secondaryButton("Ver mi perfil");
        perfil.setOnAction(e -> mostrarPerfil(cliente));
        Button actualizar = secondaryButton("Actualizar listado");
        actualizar.setOnAction(e -> refrescarTablaCliente(table, cliente));
        content.getChildren().add(new HBox(10, cancelar, calificar, perfil, actualizar));
        root.setCenter(content);
        setScene(root);
    }

    private TableView<Viaje> tablaViajes(Cliente cliente) {
        TableView<Viaje> t = new TableView<>();
        TableColumn<Viaje,String> id=col("ID",v->String.valueOf(v.getIdViaje()));
        TableColumn<Viaje,String> ori=col("Origen",Viaje::getOrigen);
        TableColumn<Viaje,String> des=col("Destino",Viaje::getDestino);
        TableColumn<Viaje,String> est=col("Estado",v->v.getEstado().toString());
        TableColumn<Viaje,String> monto=col("Importe",v->String.format("$ %.2f",v.getPrecio()));
        t.getColumns().addAll(id,ori,des,est,monto);
        refrescarTablaCliente(t,cliente);
        t.setPrefHeight(260);
        return t;
    }
    private void refrescarTablaCliente(TableView<Viaje> t, Cliente c) {
        t.setItems(FXCollections.observableArrayList(gestorViajes.listarPorClientes(c)));
    }

    private void mostrarConductor(Conductor conductor) {
        try {
            List<Vehiculo> cargados = new VehiculoDAO().listarPorConductorId(conductor.getIdUsuario());
            for (Vehiculo v : cargados) {
                if (conductor.buscarVehiculoPorId(v.getIdVehiculo()) == null) conductor.agregarVehiculo(v);
            }
            int seleccionado = conductor.getIdVehiculoSeleccionadoPersistido();
            if (seleccionado > 0 && conductor.getVehiculoSeleccionado() == null) {
                try { conductor.seleccionarVehiculo(seleccionado); } catch (Exception ignored) {}
            }
        } catch (Exception ex) { alerta("Vehículos", ex.getMessage()); }
        BorderPane root=basePanel("Área de conductor");
        VBox content=content();
        List<Viaje> propios=gestorViajes.listarPorConductores(conductor);
        double total=0;
        try {
            gestorPagos.cargarPagos();
            for(Pagos p:gestorPagos.getPagosRealizados()) {
                Viaje v=p.getViaje();
                if(v!=null && v.getConductor()!=null &&
                   v.getConductor().getIdUsuario()==conductor.getIdUsuario() &&
                   p.getEstado()!=EstadoPago.REEMBOLSADO) total+=p.calcularGananciaConductor();
            }
        } catch(Exception ignored) {}
        content.getChildren().addAll(heading("Hola, " + conductor.getNombre()),
                muted("Gestioná tu actividad y tus viajes."),
                new HBox(14,metricCard("Viajes asignados",String.valueOf(propios.size())),
                        metricCard("Ganancias acumuladas",String.format("$ %.2f",total)),
                        metricCard("Disponibilidad",conductor.isDisponible()?"Disponible":"No disponible")));
        Button disponibilidad=primaryButton(conductor.isDisponible()?"Pasar a no disponible":"Ponerme disponible");
        disponibilidad.setOnAction(e->{
            try {
                boolean nueva=!conductor.isDisponible();
                if (nueva && conductor.getVehiculoSeleccionado() == null) {
                    alerta("Seleccionar vehículo", "Antes de ponerte disponible tenés que seleccionar un vehículo.");
                    return;
                }
                new UsuarioDAO().actualizarDisponibilidad(conductor.getIdUsuario(),nueva);
                conductor.setDisponible(nueva);
                mostrarConductor(conductor);
            } catch(Exception ex){alerta("Disponibilidad",ex.getMessage());}
        });
        content.getChildren().addAll(disponibilidad,sectionTitle("Viajes disponibles"));
        TableView<Viaje> disponibles=tablaViajesGenerica(gestorViajes.listarPorEstado(EstadoViaje.PENDIENTE));
        content.getChildren().add(disponibles);
        Button aceptar=secondaryButton("Aceptar viaje seleccionado");
        aceptar.setOnAction(e->{
            Viaje v=disponibles.getSelectionModel().getSelectedItem();
            if(v==null){alerta("Seleccioná un viaje","Elegí un viaje disponible.");return;}
            try{gestorViajes.asignarConductor(v.getIdViaje(),conductor);mostrarConductor(conductor);}
            catch(Exception ex){alerta("No se pudo aceptar",ex.getMessage());}
        });
        content.getChildren().addAll(aceptar,sectionTitle("Mis viajes"));
        TableView<Viaje> mis=tablaViajesGenerica(gestorViajes.listarPorConductores(conductor));
        content.getChildren().add(mis);
        HBox acciones=new HBox(10);
        Button iniciar=secondaryButton("Iniciar viaje");
        iniciar.setOnAction(e->cambiarEstadoViaje(mis,conductor,true));
        Button finalizar=secondaryButton("Finalizar viaje");
        finalizar.setOnAction(e->cambiarEstadoViaje(mis,conductor,false));
        acciones.getChildren().addAll(iniciar,finalizar);
        Button refrescar = secondaryButton("Actualizar viajes");
        refrescar.setOnAction(e -> mostrarConductor(conductor));
        Button perfilConductor = secondaryButton("Consultar mis datos");
        perfilConductor.setOnAction(e -> mostrarPerfil(conductor));
        Button misVehiculos = secondaryButton("Consultar mis vehículos");
        misVehiculos.setOnAction(e -> dialogoMisVehiculos(conductor));
        Button registrarVehiculo = secondaryButton("Registrar vehículo");
        registrarVehiculo.setOnAction(e -> dialogoRegistrarVehiculo(conductor));
        Button seleccionarVehiculo = secondaryButton("Seleccionar vehículo");
        seleccionarVehiculo.setOnAction(e -> dialogoSeleccionarVehiculo(conductor));
        Button calificar = secondaryButton("Calificar pasajero");
        calificar.setOnAction(e -> dialogoCalificar(conductor, mis));
        Button ganancias = primaryButton("Consultar ganancias acumuladas");
        ganancias.setOnAction(e -> mostrarGanancias(conductor));
        content.getChildren().addAll(acciones,
                sectionTitle("Vehículos"), new HBox(10, misVehiculos, registrarVehiculo, seleccionarVehiculo),
                new HBox(10, calificar, ganancias, refrescar, perfilConductor));
        root.setCenter(content);
        setScene(root);
    }

    private void cambiarEstadoViaje(TableView<Viaje> tabla, Conductor conductor, boolean iniciar) {
        Viaje v=tabla.getSelectionModel().getSelectedItem();
        if(v==null){alerta("Seleccioná un viaje","Elegí uno de tus viajes.");return;}
        try {
            if(iniciar) gestorViajes.iniciarViaje(v.getIdViaje());
            else gestorViajes.finalizarViaje(v.getIdViaje());
            mostrarConductor(conductor);
        } catch(Exception ex){alerta("No se pudo actualizar el viaje",ex.getMessage());}
    }

    private void mostrarAdministrador() {
        try { gestorUsuario.cargarUsuarios(); } catch(Exception e) { alerta("Usuarios",e.getMessage()); }
        BorderPane root=basePanel("Administración");
        VBox content=content();
        List<Usuario> usuarios=gestorUsuario.listarUsuarios();
        content.getChildren().addAll(heading("Panel de administración"),
                muted("Administrá las cuentas y supervisá la actividad de TeLlevo."),
                new HBox(14,metricCard("Usuarios",String.valueOf(usuarios.size())),
                        metricCard("Estado","Sistema operativo")));
        TableView<Usuario> tabla=new TableView<>(FXCollections.observableArrayList(usuarios));
        tabla.getColumns().addAll(
                col("ID",u->String.valueOf(u.getIdUsuario())),
                col("Nombre",u->u.getNombre()+" "+u.getApellido()),
                col("Email",Usuario::getEmail),
                col("Tipo",u->u.getTipoUsuario().toString()),
                col("Estado",u->u.isActivo()?"Activo":"Inactivo"));
        tabla.setPrefHeight(320);
        Button activar=secondaryButton("Activar usuario");
        activar.setOnAction(e->cambiarEstadoUsuario(tabla,true));
        Button desactivar=secondaryButton("Desactivar usuario");
        desactivar.setOnAction(e->cambiarEstadoUsuario(tabla,false));
        Button pagos=primaryButton("Consultar pagos");
        pagos.setOnAction(e->mostrarPagos());
        Button buscar = secondaryButton("Buscar por ID");
        buscar.setOnAction(e -> dialogoBuscarUsuario());
        Button registrar = secondaryButton("Registrar cliente");
        registrar.setOnAction(e -> dialogoRegistrarCliente());
        Button modificar = secondaryButton("Modificar usuario");
        modificar.setOnAction(e -> dialogoModificarUsuario(tabla));
        Button asignar = secondaryButton("Asignar vehículo a conductor");
        asignar.setOnAction(e -> dialogoAsignarVehiculo());
        Button refrescar = secondaryButton("Actualizar listado");
        refrescar.setOnAction(e -> mostrarAdministrador());
        content.getChildren().addAll(sectionTitle("Usuarios registrados"),tabla,
                new HBox(10,activar,desactivar,pagos),
                new HBox(10,buscar,registrar,modificar),
                new HBox(10,asignar,refrescar));
        root.setCenter(content);
        setScene(root);
    }

    private void cambiarEstadoUsuario(TableView<Usuario> tabla, boolean activar) {
        Usuario u=tabla.getSelectionModel().getSelectedItem();
        if(u==null){alerta("Seleccioná un usuario","Elegí un usuario de la tabla.");return;}
        try {
            if(activar) gestorUsuario.activarUsuario(u.getIdUsuario());
            else gestorUsuario.desactivarUsuario(u.getIdUsuario());
            mostrarAdministrador();
        } catch(Exception ex){alerta("No se pudo actualizar el usuario",ex.getMessage());}
    }

    private void mostrarPagos() {
        try {
            gestorPagos.cargarPagos();
            StringBuilder sb=new StringBuilder();
            for(Pagos p:gestorPagos.getPagosRealizados()) {
                sb.append("Pago #").append(p.getIdPago())
                  .append(" | Viaje #").append(p.getViaje()==null?"-":p.getViaje().getIdViaje())
                  .append(" | $").append(String.format("%.2f",p.getMonto()))
                  .append(" | ").append(p.getMetodoPago())
                  .append(" | ").append(p.getEstado())
                  .append(" | Fecha: ").append(p.getFechaPago())
                  .append(" | Comisión TeLlevo: $").append(String.format("%.2f",p.calcularComisionPlataforma()))
                  .append(" | Ganancia conductor: $").append(String.format("%.2f",p.calcularGananciaConductor()))
                  .append("\n");
            }
            TextArea area=new TextArea(sb.length()==0?"No hay pagos registrados.":sb.toString());
            area.setEditable(false); area.setWrapText(true);
            Dialog<Void> d=new Dialog<>(); d.setTitle("Pagos registrados");
            d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            d.getDialogPane().setContent(area); d.showAndWait();
        } catch(Exception ex){alerta("Pagos",ex.getMessage());}
    }


    // Funciones que estaban disponibles en los menús de consola y se incorporan a JavaFX.

    private void mostrarPerfil(Usuario u) {
        String extra = u instanceof Conductor c
                ? "\nLicencia: " + c.getNumeroLicencia() + "\nDisponibilidad: " + (c.isDisponible() ? "Disponible" : "No disponible")
                : "";
        alerta("Mi perfil", "ID: " + u.getIdUsuario() + "\nNombre: " + u.getNombre() + " " + u.getApellido()
                + "\nDNI: " + u.getDni() + "\nEmail: " + u.getEmail() + "\nTeléfono: " + u.getTelefono()
                + "\nEstado: " + (u.isActivo() ? "Activo" : "Inactivo")
                + "\nCalificación promedio: " + u.getCalificacionPromedio()
                + "\nCantidad de calificaciones: " + u.getCantidadCalificaciones() + extra);
    }

    private void dialogoCalificar(Usuario autor, TableView<Viaje> tabla) {
        Viaje viaje = tabla.getSelectionModel().getSelectedItem();
        if (viaje == null) {
            alerta("Calificar viaje", "Seleccioná un viaje de la tabla.");
            return;
        }
        if (viaje.getEstado() != EstadoViaje.FINALIZADO) {
            alerta("Calificar viaje", "Solo se pueden calificar viajes finalizados.");
            return;
        }
        if (viaje.getCliente() == null || viaje.getConductor() == null) {
            alerta("Calificar viaje", "El viaje debe tener cliente y conductor asignados.");
            return;
        }
        boolean participa = viaje.getCliente().getIdUsuario() == autor.getIdUsuario()
                || viaje.getConductor().getIdUsuario() == autor.getIdUsuario();
        if (!participa) {
            alerta("Calificar viaje", "No participaste de este viaje.");
            return;
        }
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Calificar viaje #" + viaje.getIdViaje());
        d.getDialogPane().getButtonTypes().addAll(new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        Spinner<Integer> puntos = new Spinner<>(1, 5, 5);
        TextField comentario = field("Comentario (opcional, máximo 255 caracteres)");
        VBox box = new VBox(10, new Label("Puntuación (1 a 5 estrellas)"), puntos,
                new Label("Comentario"), comentario);
        box.setPadding(new Insets(15));
        d.getDialogPane().setContent(box);
        d.showAndWait().ifPresent(r -> {
            if (r.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    gestorCalificaciones.calificarViaje(viaje, autor, puntos.getValue(), comentario.getText());
                    alerta("Calificación guardada", "La calificación se registró correctamente.");
                    if (autor instanceof Cliente c) mostrarCliente(c);
                    else if (autor instanceof Conductor c) mostrarConductor(c);
                } catch (Exception ex) {
                    alerta("No se pudo calificar", ex.getMessage());
                }
            }
        });
    }

    private void dialogoMisVehiculos(Conductor c) {
        try {
            List<Vehiculo> vs = new VehiculoDAO().listarPorConductorId(c.getIdUsuario());
            for (Vehiculo v : vs) {
                if (c.buscarVehiculoPorId(v.getIdVehiculo()) == null) c.agregarVehiculo(v);
            }
            StringBuilder sb = new StringBuilder();
            for (Vehiculo v : vs) {
                sb.append("ID: ").append(v.getIdVehiculo()).append(" | ")
                  .append(v instanceof Auto ? "Auto" : "Moto").append(" | ")
                  .append(v.getPatente()).append(" | ").append(v.getMarca()).append(" ")
                  .append(v.getModelo()).append(" | Año: ").append(v.getAnio())
                  .append(" | Color: ").append(v.getColor()).append(" | Activo: ")
                  .append(v.isActivo());
                if (v instanceof Auto a) sb.append(" | Puertas: ").append(a.getCantPuertas());
                if (v instanceof Moto m) sb.append(" | Cilindrada: ").append(m.getCilindrada()).append(" cc");
                sb.append("\n");
            }
            alerta("Mis vehículos", sb.length() == 0 ? "Todavía no tenés vehículos registrados." : sb.toString());
        } catch (Exception ex) { alerta("Vehículos", ex.getMessage()); }
    }

    private void dialogoRegistrarVehiculo(Conductor c) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Registrar vehículo");
        d.getDialogPane().getButtonTypes().addAll(new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Auto", "Moto"));
        tipo.getSelectionModel().selectFirst();
        TextField patente = field("Patente"), marca = field("Marca"), modelo = field("Modelo"),
                anio = field("Año"), color = field("Color"), detalle = field("Puertas (auto) / cilindrada (moto)");
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(9); g.setPadding(new Insets(15));
        g.addRow(0, new Label("Tipo"), tipo); g.addRow(1, new Label("Patente"), patente);
        g.addRow(2, new Label("Marca"), marca); g.addRow(3, new Label("Modelo"), modelo);
        g.addRow(4, new Label("Año"), anio); g.addRow(5, new Label("Color"), color);
        g.addRow(6, new Label("Puertas / cilindrada"), detalle);
        d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(r -> {
            if (r.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    int year = Integer.parseInt(anio.getText().trim());
                    int extra = Integer.parseInt(detalle.getText().trim());
                    Vehiculo v = tipo.getValue().equals("Auto")
                            ? new Auto(patente.getText().trim(), marca.getText().trim(), modelo.getText().trim(), year, color.getText().trim(), extra)
                            : new Moto(patente.getText().trim(), marca.getText().trim(), modelo.getText().trim(), year, color.getText().trim(), extra);
                    new VehiculoDAO().insertar(v, c.getIdUsuario());
                    c.agregarVehiculo(v);
                    alerta("Vehículo registrado", "Se registró correctamente. ID asignado: " + v.getIdVehiculo());
                    mostrarConductor(c);
                } catch (Exception ex) { alerta("No se pudo registrar el vehículo", ex.getMessage()); }
            }
        });
    }

    private void dialogoSeleccionarVehiculo(Conductor c) {
        try {
            List<Vehiculo> vs = new VehiculoDAO().listarPorConductorId(c.getIdUsuario());
            if (vs.isEmpty()) { alerta("Seleccionar vehículo", "Primero registrá un vehículo."); return; }
            ChoiceDialog<Vehiculo> d = new ChoiceDialog<>(vs.get(0), vs);
            d.setTitle("Seleccionar vehículo");
            d.setHeaderText("Elegí el vehículo con el que vas a trabajar");
            d.setContentText("Vehículo:");
            d.showAndWait().ifPresent(v -> {
                try {
                    c.seleccionarVehiculo(v.getIdVehiculo());
                    boolean ok = new UsuarioDAO().actualizarVehiculoSeleccionado(c.getIdUsuario(), v.getIdVehiculo());
                    if (!ok) throw new IllegalStateException("No se pudo guardar el vehículo seleccionado.");
                    alerta("Vehículo seleccionado", "Vehículo activo: " + v.getPatente());
                    mostrarConductor(c);
                } catch (Exception ex) { alerta("No se pudo seleccionar", ex.getMessage()); }
            });
        } catch (Exception ex) { alerta("Vehículos", ex.getMessage()); }
    }

    private void mostrarGanancias(Conductor c) {
        try {
            gestorPagos.cargarPagos();
            double bruto = 0, comision = 0, neto = 0;
            int cantidad = 0;
            for (Pagos p : gestorPagos.getPagosRealizados()) {
                Viaje v = p.getViaje();
                if (v != null && v.getConductor() != null
                        && v.getConductor().getIdUsuario() == c.getIdUsuario()
                        && p.getEstado() != EstadoPago.REEMBOLSADO) {
                    bruto += p.getMonto();
                    comision += p.calcularComisionPlataforma();
                    neto += p.calcularGananciaConductor();
                    cantidad++;
                }
            }
            alerta("Mis ganancias acumuladas", "Pagos considerados: " + cantidad
                    + "\nTotal de viajes: " + gestorViajes.listarPorConductores(c).size()
                    + "\nImporte total: $" + String.format("%.2f", bruto)
                    + "\nComisión de TeLlevo: $" + String.format("%.2f", comision)
                    + "\nTu ganancia (75%): $" + String.format("%.2f", neto));
        } catch (Exception ex) { alerta("Ganancias", ex.getMessage()); }
    }

    private void dialogoBuscarUsuario() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Buscar usuario"); d.setHeaderText("Buscar usuario por ID"); d.setContentText("ID:");
        d.showAndWait().ifPresent(value -> {
            try {
                Usuario u = gestorUsuario.buscarUsuarioPorId(Integer.parseInt(value.trim()));
                mostrarPerfil(u);
            } catch (Exception ex) { alerta("Búsqueda", ex.getMessage()); }
        });
    }

    private void dialogoRegistrarCliente() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Registrar cliente");
        d.getDialogPane().getButtonTypes().addAll(new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        TextField nombre=field("Nombre"), apellido=field("Apellido"), dni=field("DNI"),
                email=field("Email"), telefono=field("Teléfono");
        PasswordField pass=new PasswordField(); pass.setPromptText("Contraseña");
        GridPane g=new GridPane(); g.setHgap(10); g.setVgap(9); g.setPadding(new Insets(15));
        g.addRow(0,new Label("Nombre"),nombre); g.addRow(1,new Label("Apellido"),apellido);
        g.addRow(2,new Label("DNI"),dni); g.addRow(3,new Label("Email"),email);
        g.addRow(4,new Label("Teléfono"),telefono); g.addRow(5,new Label("Contraseña"),pass);
        d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(r -> {
            if (r.getButtonData()==ButtonBar.ButtonData.OK_DONE) {
                try {
                    Cliente nuevo=new Cliente(nombre.getText(),apellido.getText(),dni.getText(),email.getText(),telefono.getText(),pass.getText());
                    gestorUsuario.registrarUsuario(nuevo);
                    alerta("Cliente registrado","El cliente fue registrado correctamente.");
                    mostrarAdministrador();
                } catch(Exception ex){ alerta("Registro",ex.getMessage()); }
            }
        });
    }

    private void dialogoModificarUsuario(TableView<Usuario> tabla) {
        Usuario u=tabla.getSelectionModel().getSelectedItem();
        if(u==null){ alerta("Modificar usuario","Seleccioná un usuario de la tabla."); return; }
        Dialog<ButtonType> d=new Dialog<>(); d.setTitle("Modificar usuario #"+u.getIdUsuario());
        d.getDialogPane().getButtonTypes().addAll(new ButtonType("Guardar cambios",ButtonBar.ButtonData.OK_DONE),ButtonType.CANCEL);
        TextField n=field("Nombre"), a=field("Apellido"), dni=field("DNI"), em=field("Email"), tel=field("Teléfono");
        PasswordField pass=new PasswordField(); pass.setPromptText("Nueva contraseña");
        n.setText(u.getNombre()); a.setText(u.getApellido()); dni.setText(u.getDni()); em.setText(u.getEmail()); tel.setText(u.getTelefono()); pass.setText(u.getPassword());
        GridPane g=new GridPane(); g.setHgap(10); g.setVgap(9); g.setPadding(new Insets(15));
        g.addRow(0,new Label("Nombre"),n);g.addRow(1,new Label("Apellido"),a);g.addRow(2,new Label("DNI"),dni);
        g.addRow(3,new Label("Email"),em);g.addRow(4,new Label("Teléfono"),tel);g.addRow(5,new Label("Contraseña"),pass);
        d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(r->{ if(r.getButtonData()==ButtonBar.ButtonData.OK_DONE){
            try { gestorUsuario.modificarUsuario(u.getIdUsuario(),n.getText(),a.getText(),dni.getText(),em.getText(),tel.getText(),pass.getText()); mostrarAdministrador(); }
            catch(Exception ex){ alerta("Modificación",ex.getMessage()); }
        }});
    }

    private void dialogoAsignarVehiculo() {
        Dialog<ButtonType> d=new Dialog<>(); d.setTitle("Asignar vehículo a conductor");
        d.getDialogPane().getButtonTypes().addAll(new ButtonType("Asignar",ButtonBar.ButtonData.OK_DONE),ButtonType.CANCEL);
        TextField idC=field("ID del conductor"), idV=field("ID del vehículo");
        GridPane g=new GridPane();g.setHgap(10);g.setVgap(9);g.setPadding(new Insets(15));
        g.addRow(0,new Label("ID conductor"),idC);g.addRow(1,new Label("ID vehículo"),idV);d.getDialogPane().setContent(g);
        d.showAndWait().ifPresent(r->{if(r.getButtonData()==ButtonBar.ButtonData.OK_DONE){
            try {
                int cid=Integer.parseInt(idC.getText().trim()), vid=Integer.parseInt(idV.getText().trim());
                Usuario u=gestorUsuario.buscarUsuarioPorId(cid);
                if(!(u instanceof Conductor)) throw new IllegalArgumentException("El usuario seleccionado no es un conductor.");
                boolean ok=new VehiculoDAO().asignarAConductor(vid,cid);
                alerta("Asignación",ok?"Vehículo asignado correctamente.":"No se pudo asignar: verificá que el vehículo y el conductor cumplan las condiciones.");
            } catch(Exception ex){ alerta("Asignación",ex.getMessage()); }
        }});
    }

    private TableView<Viaje> tablaViajesGenerica(List<Viaje> viajes) {
        TableView<Viaje> t=new TableView<>(FXCollections.observableArrayList(viajes));
        t.getColumns().addAll(col("ID",v->String.valueOf(v.getIdViaje())),
                col("Origen",Viaje::getOrigen),col("Destino",Viaje::getDestino),
                col("Estado",v->v.getEstado().toString()),
                col("Importe",v->String.format("$ %.2f",v.getPrecio())));
        t.setPrefHeight(210);
        return t;
    }

    private <T> TableColumn<T,String> col(String title, java.util.function.Function<T,String> value) {
        TableColumn<T,String> c=new TableColumn<>(title);
        c.setCellValueFactory(data->new javafx.beans.property.SimpleStringProperty(
                value.apply(data.getValue())==null?"":value.apply(data.getValue())));
        c.setPrefWidth(150);
        return c;
    }

    private VBox content() {
        VBox v=new VBox(15); v.setPadding(new Insets(28)); v.setFillWidth(true);
        return v;
    }
    private Label heading(String text) {
        Label l=new Label(text); l.setStyle("-fx-text-fill:"+TEXTO+";-fx-font-size:27px;-fx-font-weight:700;"); return l;
    }
    private Label sectionTitle(String text) {
        Label l=new Label(text); l.setStyle("-fx-text-fill:"+VERDE+";-fx-font-size:17px;-fx-font-weight:700;"); return l;
    }
    private Label muted(String text) {
        Label l=new Label(text); l.setStyle("-fx-text-fill:#788174;-fx-font-size:13px;"); return l;
    }
    private TextField field(String prompt) {
        TextField f=new TextField(); f.setPromptText(prompt); f.setPrefHeight(38); f.setPrefWidth(280); return f;
    }
    private Button primaryButton(String text) {
        Button b=new Button(text); b.setStyle("-fx-background-color:"+OLIVA+";-fx-text-fill:white;-fx-font-weight:700;-fx-background-radius:8;-fx-padding:11 18;"); return b;
    }
    private Button secondaryButton(String text) {
        Button b=new Button(text); b.setStyle("-fx-background-color:#E3E7DA;-fx-text-fill:"+VERDE+";-fx-font-weight:600;-fx-background-radius:8;-fx-padding:10 15;"); return b;
    }
    private VBox metricCard(String title, String value) {
        VBox b=new VBox(7); b.setPadding(new Insets(16)); b.setMinWidth(180);
        b.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#E1E4D9;-fx-border-radius:12;");
        Label t=muted(title); Label v=new Label(value);
        v.setStyle("-fx-text-fill:"+VERDE+";-fx-font-size:20px;-fx-font-weight:700;");
        b.getChildren().addAll(t,v); return b;
    }
    private void setScene(BorderPane root) {
        ScrollPane scroll=new ScrollPane(root); scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll,1100,760);
        scene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    private void alerta(String titulo, String mensaje) {
        Alert a=new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje==null?"Ocurrió un error.":mensaje);
        a.showAndWait();
    }
}
