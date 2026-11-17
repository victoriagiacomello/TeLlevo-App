
package menus;

import excepciones.AccesoDatosException;
import excepciones.ElementoNoEncontradoException;
import excepciones.PasajeroInvalidoException;

import gestores.GestorUsuario;
import gestores.GestorPagos;

import modelo.Cliente;
import modelo.Usuario;
import modelo.Pagos;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MenuAdministrador {

    private Scanner scanner;
    private GestorUsuario gestorUsuario;
    private GestorPagos gestorPagos;

    public MenuAdministrador(GestorUsuario gestorUsuario, Scanner scanner) {

        this.gestorUsuario = gestorUsuario;
        this.scanner = scanner;
        this.gestorPagos = new GestorPagos();
    }

    // MENU PRINCIPAL DEL ADMINISTRADOR
    public void mostrarMenu() {

        int opcion = 0;

        while (opcion != 9) {

            System.out.println("\n===== MENU ADMINISTRADOR =====");
            System.out.println("1. Listar usuarios");
            System.out.println("2. Buscar usuario por ID");
            System.out.println("3. Registrar cliente");
            System.out.println("4. Modificar usuario");
            System.out.println("5. Desactivar usuario");
            System.out.println("6. Activar usuario");
            System.out.println("7. Asignar vehiculo a conductor");
            System.out.println("8. Listar todos los pagos");
            System.out.println("9. Volver al menu principal");

            System.out.print("Seleccione una opcion: ");

            if (scanner.hasNextInt()) {

                opcion = scanner.nextInt();
                scanner.nextLine();

            } else {

                System.out.println("Debe ingresar un numero.");
                scanner.nextLine();
                continue;
            }

            switch (opcion) {

                case 1:
                    listarUsuarios();
                    break;

                case 2:
                    buscarUsuario();
                    break;

                case 3:
                    registrarCliente();
                    break;

                case 4:
                    modificarUsuario();
                    break;

                case 5:
                    desactivarUsuario();
                    break;

                case 6:
                    activarUsuario();
                    break;

                case 7:
                    asignarVehiculo();
                    break;

                case 8:
                    listarPagos();
                    break;

                case 9:
                    System.out.println("Volviendo al menu principal...");
                    break;

                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    // LISTAR USUARIOS
    private void listarUsuarios() {

        List<Usuario> usuarios = gestorUsuario.listarUsuarios();

        if (usuarios.isEmpty()) {

            System.out.println("\nNo hay usuarios registrados.");

        } else {

            System.out.println("\n===== LISTADO DE USUARIOS =====");

            for (Usuario usuario : usuarios) {

                System.out.println("\n-----------------------------------");
                System.out.println("ID de usuario: " + usuario.getIdUsuario());

                usuario.mostrarDatos();

                System.out.println("-----------------------------------");
            }
        }
    }

    // BUSCAR USUARIO POR ID
    private void buscarUsuario() {

        int id = leerId();

        try {

            Usuario usuario = gestorUsuario.buscarUsuarioPorId(id);

            System.out.println("\n===== USUARIO ENCONTRADO =====");
            System.out.println("ID de usuario: " + usuario.getIdUsuario());

            usuario.mostrarDatos();

        } catch (ElementoNoEncontradoException e) {

            System.out.println(e.getMessage());
        }
    }

    // REGISTRAR CLIENTE
    private void registrarCliente() {

        System.out.println("\n===== REGISTRAR CLIENTE =====");

        String nombre = leerCampo("Nombre: ");
        String apellido = leerCampo("Apellido: ");
        String dni = leerCampo("DNI: ");
        String email = leerCampo("Email: ");
        String telefono = leerCampo("Telefono: ");
        String password = leerCampo("Password: ");

        try {

            Cliente cliente = new Cliente(
                    nombre,
                    apellido,
                    dni,
                    email,
                    telefono,
                    password
            );

            gestorUsuario.registrarUsuario(cliente);

            System.out.println("\nCliente registrado correctamente.");

        } catch (PasajeroInvalidoException e) {

            System.out.println("Datos invalidos: " + e.getMessage());

        } catch (AccesoDatosException e) {

            System.out.println("Error en la base de datos: " + e.getMessage());

        } catch (SQLException e) {

            System.out.println("Error SQL: " + e.getMessage());
        }
    }

    // MODIFICAR USUARIO
    private void modificarUsuario() {

        System.out.println("\n===== MODIFICAR USUARIO =====");

        int id = leerId();

        try {

            Usuario usuario = gestorUsuario.buscarUsuarioPorId(id);

            System.out.println("\nUsuario encontrado:");
            usuario.mostrarDatos();

            System.out.println("\nIngrese los nuevos datos:");

            String nombre = leerCampo("Nombre: ");
            String apellido = leerCampo("Apellido: ");
            String dni = leerCampo("DNI: ");
            String email = leerCampo("Email: ");
            String telefono = leerCampo("Telefono: ");
            String password = leerCampo("Password: ");

            boolean actualizado = gestorUsuario.modificarUsuario(
                    id,
                    nombre,
                    apellido,
                    dni,
                    email,
                    telefono,
                    password
            );

            if (actualizado) {

                System.out.println("\nUsuario modificado correctamente.");

            } else {

                System.out.println("\nNo se pudo modificar el usuario.");
            }

        } catch (PasajeroInvalidoException e) {

            System.out.println("Datos invalidos: " + e.getMessage());

        } catch (IllegalArgumentException e) {

            System.out.println("Datos invalidos: " + e.getMessage());

        } catch (ElementoNoEncontradoException e) {

            System.out.println("Usuario no encontrado: " + e.getMessage());

        } catch (AccesoDatosException e) {

            System.out.println("Error en la base de datos: " + e.getMessage());

        } catch (SQLException e) {

            System.out.println("Error SQL: " + e.getMessage());
        }
    }

    // DESACTIVAR USUARIO
    private void desactivarUsuario() {

        System.out.println("\n===== DESACTIVAR USUARIO =====");

        int id = leerId();

        try {

            Usuario usuario = gestorUsuario.buscarUsuarioPorId(id);

            System.out.println("\nUsuario seleccionado:");
            usuario.mostrarDatos();

            if (!usuario.isActivo()) {

                System.out.println("\nEl usuario ya se encuentra desactivado.");
                return;
            }

            String confirmacion;

            do {

                System.out.print("\n¿Confirma la desactivacion? (S/N): ");
                confirmacion = scanner.nextLine().trim();

                if (!confirmacion.equalsIgnoreCase("S")
                        && !confirmacion.equalsIgnoreCase("N")) {

                    System.out.println("Respuesta invalida. Ingrese S o N.");
                }

            } while (!confirmacion.equalsIgnoreCase("S")
                    && !confirmacion.equalsIgnoreCase("N"));

            if (confirmacion.equalsIgnoreCase("S")) {

                boolean desactivado = gestorUsuario.desactivarUsuario(id);

                if (desactivado) {

                    System.out.println("\nUsuario desactivado correctamente.");

                } else {

                    System.out.println("\nNo se pudo desactivar el usuario.");
                }

            } else {

                System.out.println("\nOperacion cancelada.");
            }

        } catch (ElementoNoEncontradoException e) {

            System.out.println("Usuario no encontrado: " + e.getMessage());

        } catch (AccesoDatosException e) {

            System.out.println("Error en la base de datos: " + e.getMessage());

        } catch (SQLException e) {

            System.out.println("Error SQL: " + e.getMessage());
        }
    }

    // ACTIVAR USUARIO
    private void activarUsuario() {

        System.out.println("\n===== ACTIVAR USUARIO =====");

        int id = leerId();

        try {

            Usuario usuario = gestorUsuario.buscarUsuarioPorId(id);

            System.out.println("\nUsuario seleccionado:");
            usuario.mostrarDatos();

            if (usuario.isActivo()) {

                System.out.println("\nEl usuario ya se encuentra activo.");
                return;
            }

            String confirmacion;

            do {

                System.out.print("\n¿Confirma la activacion? (S/N): ");
                confirmacion = scanner.nextLine().trim();

                if (!confirmacion.equalsIgnoreCase("S")
                        && !confirmacion.equalsIgnoreCase("N")) {

                    System.out.println("Respuesta invalida. Ingrese S o N.");
                }

            } while (!confirmacion.equalsIgnoreCase("S")
                    && !confirmacion.equalsIgnoreCase("N"));

            if (confirmacion.equalsIgnoreCase("S")) {

                boolean activado = gestorUsuario.activarUsuario(id);

                if (activado) {

                    System.out.println("\nUsuario activado correctamente.");

                } else {

                    System.out.println("\nNo se pudo activar el usuario.");
                }

            } else {

                System.out.println("\nOperacion cancelada.");
            }

        } catch (ElementoNoEncontradoException e) {

            System.out.println("Usuario no encontrado: " + e.getMessage());

        } catch (AccesoDatosException e) {

            System.out.println("Error en la base de datos: " + e.getMessage());

        } catch (SQLException e) {

            System.out.println("Error SQL: " + e.getMessage());
        }
    }

    // LISTAR TODOS LOS PAGOS
    private void listarPagos() {

        System.out.println("\n===== LISTADO DE PAGOS =====");

        try {

            // Cargamos los pagos desde la base de datos.
            gestorPagos.cargarPagos();

            List<Pagos> pagos = gestorPagos.getPagosRealizados();

            if (pagos.isEmpty()) {

                System.out.println("\nNo hay pagos registrados.");
                return;
            }

            for (Pagos pago : pagos) {

                System.out.println("\n-----------------------------------");
                System.out.println("ID del pago: " + pago.getIdPago());
                System.out.println("ID del viaje: " +
                        pago.getViaje().getIdViaje());
                System.out.println("Monto: $" + pago.getMonto());
                System.out.println("Fecha del pago: " + pago.getFechaPago());
                System.out.println("Metodo de pago: " + pago.getMetodoPago());
                System.out.println("Estado del pago: " + pago.getEstado());

                System.out.println("-----------------------------------");
                System.out.println("Comision de la plataforma: $" +
                        pago.calcularComisionPlataforma());

                System.out.println("Ganancia del conductor: $" +
                        pago.calcularGananciaConductor());

                System.out.println("-----------------------------------");
            }

        } catch (Exception e) {

            System.out.println("\nNo se pudieron listar los pagos: " +
                    e.getMessage());
        }
    }

    // LEER ID
    private int leerId() {

        int id;

        while (true) {

            System.out.print("Ingrese el ID del usuario: ");

            if (scanner.hasNextInt()) {

                id = scanner.nextInt();
                scanner.nextLine();

                if (id > 0) {

                    return id;
                }

                System.out.println("El ID debe ser un numero positivo.");

            } else {

                System.out.println("Debe ingresar un ID numerico.");
                scanner.nextLine();
            }
        }
    }

    // LEER CAMPO DE TEXTO
    private String leerCampo(String mensaje) {

        String valor;

        do {

            System.out.print(mensaje);
            valor = scanner.nextLine().trim();

            if (valor.isEmpty()) {

                System.out.println("Este campo no puede estar vacio.");
            }

        } while (valor.isEmpty());

        return valor;
    }

    // ASIGNAR VEHICULO A CONDUCTOR
    private void asignarVehiculo() {

        System.out.println("\n===== ASIGNAR VEHICULO A CONDUCTOR =====");

        int idConductor = leerId();
        int idVehiculo;

        try {

            Usuario usuario = gestorUsuario.buscarUsuarioPorId(idConductor);

            if (!(usuario instanceof modelo.Conductor)) {

                System.out.println(
                        "\nEl usuario seleccionado no es un conductor."
                );

                return;
            }

            System.out.println("\nConductor seleccionado:");
            usuario.mostrarDatos();

            System.out.print("\nIngrese el ID del vehiculo: ");

            if (!scanner.hasNextInt()) {

                System.out.println("Debe ingresar un ID numerico.");
                scanner.nextLine();
                return;
            }

            idVehiculo = scanner.nextInt();
            scanner.nextLine();

            if (idVehiculo <= 0) {

                System.out.println("El ID del vehiculo debe ser positivo.");
                return;
            }

            repositorio.VehiculoDAO vehiculoDAO =
                    new repositorio.VehiculoDAO();

            boolean asignado = vehiculoDAO.asignarAConductor(
                    idVehiculo,
                    idConductor
            );

            if (asignado) {

                System.out.println("\nVehiculo asignado correctamente.");

            } else {

                System.out.println("\nNo se encontro un vehiculo con ese ID.");
            }

        } catch (ElementoNoEncontradoException e) {

            System.out.println("Conductor no encontrado: " + e.getMessage());

        } catch (SQLException e) {

            System.out.println("Error al asignar el vehiculo: " + e.getMessage());
        }
    }
}
