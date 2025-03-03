package negocio;

import java.util.Scanner;
import datos.ConexionBD;

public class Menu {
    private Scanner scanner = new Scanner(System.in);
    private ConexionBD conexion;
    private String[] connectionData;
    private PasswordManager passwordManager;

    public void iniciar() {
        String tipoBD = seleccionarBaseDatos();
        if (tipoBD == null) {
            System.out.println("\nSaliendo del programa...");
            return;
        }

        try {
            conexion = new ConexionBD(tipoBD);
            System.out.print("\n¿Desea usar una conexión remota? (S/N): ");
            String respuesta = scanner.nextLine();
            if (respuesta.equalsIgnoreCase("S")) {
                conexion.setRemote(true);
                connectionData = pedirDatos();
                conexion.setConnectionData(connectionData);
                System.out.println("\nIntentando establecer conexión remota...");
                try {
                    if ("mongodb".equals(tipoBD)) {
                        conexion.getMongoCollection();
                    } else {
                        conexion.getConnection();
                    }
                    System.out.println("\n¡Conexión remota establecida con éxito!");
                } catch (Exception e) {
                    System.err.println("\nError al establecer la conexión remota: " + e.getMessage());
                    System.out.println("Regresando al menú de selección de base de datos...");
                    iniciar();
                    return;
                }
            }
            passwordManager = new PasswordManager(conexion, scanner);
            passwordManager.mostrarMenuPrincipal(tipoBD, this);
        } catch (Exception e) {
            System.err.println("\nError al iniciar la conexión:");
            e.printStackTrace();
        } finally {
            if (conexion != null) {
                conexion.closeConnection();
            }
            scanner.close();
        }
    }

    public String[] pedirDatos() {
        String ip, nombre, rolName, rolPassword;
        while (true) {
            System.out.print("\nIngrese la IP remota: ");
            ip = scanner.nextLine().trim();
            if (!ip.isEmpty()) {
                break;
            }
            System.out.println("Error: La IP remota no puede estar vacía. Intente de nuevo.");
        }
        while (true) {
            System.out.print("Ingrese el nombre de la base de datos: ");
            nombre = scanner.nextLine().trim();
            if (!nombre.isEmpty()) {
                break;
            }
            System.out.println("Error: El nombre de la base de datos no puede estar vacío. Intente de nuevo.");
        }
        while (true) {
            System.out.print("Ingrese el nombre del rol: ");
            rolName = scanner.nextLine().trim();
            if (!rolName.isEmpty()) {
                break;
            }
            System.out.println("Error: El nombre del rol no puede estar vacío. Intente de nuevo.");
        }

        while (true) {
            System.out.print("Ingrese la contraseña del rol: ");
            rolPassword = scanner.nextLine().trim();
            if (!rolPassword.isEmpty()) {
                break;
            }
            System.out.println("Error: La contraseña no puede estar vacía. Intente de nuevo.");
        }
        return new String[] { ip, nombre, rolName, rolPassword };
    }

    private String seleccionarBaseDatos() {
        while (true) {
            System.out.println(centrarTexto("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|"));
            System.out.println(centrarTexto("|                                             |"));
            System.out.println(centrarTexto("|                  BIENVENIDO                 |"));
            System.out.println(centrarTexto("|                                             |"));            
            System.out.println(centrarTexto("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|"));
            System.out.println();
            System.out.println(centrarTexto("| Seleccione la base de datos a conectar:     |"));
            System.out.println(centrarTexto("|                                             |"));
            System.out.println(centrarTexto("| 1. PostgreSQL                               |"));
            System.out.println(centrarTexto("| 2. MySQL                                    |"));
            System.out.println(centrarTexto("| 3. MongoDB                                  |"));
            System.out.println(centrarTexto("| 4. Salir                                    |"));
            System.out.println(centrarTexto("|                                             |"));
            System.out.println(centrarTexto("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|"));
            System.out.print(centrarTexto("Seleccione una opción: "));
            String option = scanner.nextLine();

            try {
                int opcion = Integer.parseInt(option);
                switch (opcion) {
                    case 1:
                        return "postgresql";
                    case 2:
                        return "mysql";
                    case 3:
                        return "mongodb";
                    case 4:
                        return null;
                    default:
                        System.out.println(centrarTexto("Opción no válida. Intente de nuevo."));
                }
            } catch (NumberFormatException e) {
                System.out.println(centrarTexto("Entrada no válida. Ingrese un número."));
            }
        }
    }

    private String centrarTexto(String texto) {
        int ancho = 80; 
        int espacios = (ancho - texto.length()) / 2;
        String padding = " ".repeat(Math.max(0, espacios));
        return padding + texto + padding;
    }
}
