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
            System.out.println("\nExiting the program...");
            return;
        }

        try {
            conexion = new ConexionBD(tipoBD);
            System.out.print("\nDo you want to use a remote connection? (Y/N): ");
            String respuesta = scanner.nextLine();
            if (respuesta.equalsIgnoreCase("Y")) {
                conexion.setRemote(true);
                connectionData = pedirDatos();
                conexion.setConnectionData(connectionData);
                System.out.println("\nAttempting to establish remote connection...");
                try {
                    if ("mongodb".equals(tipoBD)) {
                        conexion.getMongoCollection();
                    } else {
                        conexion.getConnection();
                    }
                    System.out.println("\nRemote connection established successfully!");
                } catch (Exception e) {
                    System.err.println("\nError establishing remote connection: " + e.getMessage());
                    System.out.println("Returning to the database selection menu...");
                    iniciar();
                    return;
                }
            }
            passwordManager = new PasswordManager(conexion, scanner);
            passwordManager.mostrarMenuPrincipal(tipoBD, this);
        } catch (Exception e) {
            System.err.println("\nError initializing the connection:");
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
            System.out.print("\nEnter the remote IP: ");
            ip = scanner.nextLine().trim();
            if (!ip.isEmpty()) {
                break;
            }
            System.out.println("Error: The remote IP cannot be empty. Please try again.");
        }
        while (true) {
            System.out.print("Enter the database name: ");
            nombre = scanner.nextLine().trim();
            if (!nombre.isEmpty()) {
                break;
            }
            System.out.println("Error: The database name cannot be empty. Please try again.");
        }
        while (true) {
            System.out.print("Enter the role name: ");
            rolName = scanner.nextLine().trim();
            if (!rolName.isEmpty()) {
                break;
            }
            System.out.println("Error: The role name cannot be empty. Please try again.");
        }

        while (true) {
            System.out.print("Enter the password for the role: ");
            rolPassword = scanner.nextLine().trim();
            if (!rolPassword.isEmpty()) {
                break;
            }
            System.out.println("Error: The password cannot be empty. Please try again.");
        }
        return new String[] { ip, nombre, rolName, rolPassword };
    }

    private String seleccionarBaseDatos() {
        while (true) {
            System.out.println("\n|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|                           WELCOME!                         |");
            System.out.println("|                      PASSWORD MANAGER                      |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|  Select the database you want to connect to:               |");
            System.out.println("|                                                            |");
            System.out.println("|  1. PostgreSQL                                             |");
            System.out.println("|  2. MySQL                                                  |");
            System.out.println("|  3. MongoDB                                                |");
            System.out.println("|  4. Exit                                                   |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.print("Select an option: ");
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
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a number.");
            }
        }
    }
}