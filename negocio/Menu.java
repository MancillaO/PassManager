package negocio;

import java.util.Scanner;
import datos.ConexionBD;

public class Menu {
    private Scanner scanner = new Scanner(System.in);
    private ConexionBD conexion;

    public void iniciar() {
        String tipoBD = seleccionarBaseDatos();
        if (tipoBD == null) {
            System.out.println("\nExiting the program...");
            return;
        }
        try {
            conexion = new ConexionBD(tipoBD);
            mostrarMenuPrincipal(tipoBD);
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
                        System.out.println("\nInvalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a number.");
            }
        }
    }

    private void mostrarMenuPrincipal(String tipoBD) {
        int opcion = 0;
        boolean esMongoDB = "mongodb".equalsIgnoreCase(tipoBD);

        while (opcion != 7) {
            System.out.println("\n|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|                      PASSWORD MANAGER                      |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|  1. View all passwords                                     |");
            System.out.println("|  2. Add new password                                       |");
            if (!esMongoDB) {
                System.out.println("|  3. Search password by service                             |");
                System.out.println("|  4. Update password                                        |");
                System.out.println("|  5. Delete password                                        |");
            }
            System.out.println("|                                                            |");
            System.out.println("|  6. Change database                                        |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.print("Select an option: ");
            String option = scanner.nextLine();

            try {
                opcion = Integer.parseInt(option);
                switch (opcion) {
                    case 1:
                        conexion.getAllPasswords();
                        break;
                    case 2:
                        addNewPassword();
                        break;
                    case 3:
                        if (!esMongoDB) {
                            searchByService();
                        } else {
                            System.out.println("\nInvalid option. Please try again.");
                        }
                        break;
                    case 4:
                        if (!esMongoDB) {
                            updatePassword();
                        } else {
                            System.out.println("\nInvalid option. Please try again.");
                        }
                        break;
                    case 5:
                        if (!esMongoDB) {
                            deletePassword();
                        } else {
                            System.out.println("\nInvalid option. Please try again.");
                        }
                        break;
                    case 6:
                        conexion.closeConnection();
                        String newDbType = seleccionarBaseDatos();
                        if (newDbType == null) {
                            System.out.println("\nExiting the program...");
                            return;
                        } else {
                            tipoBD = newDbType;
                            esMongoDB = "mongodb".equalsIgnoreCase(tipoBD);
                            conexion = new ConexionBD(tipoBD);
                        }
                        break;
                    default:
                        System.out.println("\nInvalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a number.");
            }
        }
    }

    private void searchByService() {
        System.out.print("\nEnter the service name to search: ");
        String servicio = scanner.nextLine();
        conexion.findPasswordsByService(servicio);
    }

    private void addNewPassword() {
        System.out.print("\nEnter service name: ");
        String servicio = scanner.nextLine();
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        conexion.insertPassword(servicio, username, password);
    }

    private void updatePassword() {
        System.out.print("\nEnter the ID of the password to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter new service name: ");
            String servicio = scanner.nextLine();

            System.out.print("Enter new username: ");
            String username = scanner.nextLine();

            System.out.print("Enter new password: ");
            String password = scanner.nextLine();

            conexion.updatePassword(id, servicio, username, password);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid ID format. Please enter a number.");
        }
    }

    private void deletePassword() {
        System.out.print("\nEnter the ID of the password to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            conexion.deletePassword(id);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid ID format. Please enter a number.");
        }
    }
}