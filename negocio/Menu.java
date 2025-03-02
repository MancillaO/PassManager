package negocio;

import java.util.List;
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
                        System.out.println("\nInvalid option. Please try again.\n");
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
                        showPasswords();
                        break;
                    case 2:
                        addPassword();
                        break;
                    case 3:
                        if (!esMongoDB) {
                            searchByService();
                        } else {
                            System.out.println("\nInvalid option. Please try again.\n");
                        }
                        break;
                    case 4:
                        if (!esMongoDB) {
                            updatePassword();
                        } else {
                            System.out.println("\nInvalid option. Please try again.\n");
                        }
                        break;
                    case 5:
                        if (!esMongoDB) {
                            allPasswords();
                            deletePassword();
                        } else {
                            System.out.println("\nInvalid option. Please try again.\n");
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
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a number.");
            }
        }
    }

    private void allPasswords() {
        System.out.println("\n\n|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        System.out.println("|                                                            |");
        System.out.println("|                        ALL PASSWORDS                       |");
        System.out.println("|                                                            |");
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        System.out.println("|                                                            |");
        conexion.getAllPasswords();
        System.out.println("|                                                            |");
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
    }

    private void showPasswords() {
        allPasswords();
        System.out.print(
                "Type (D) to delete a password, (U) to update a password \nOr any other key to return to the main menu: ");
        String option = scanner.nextLine();

        if (option.equalsIgnoreCase("D")) {
            deletePassword();
        } else if (option.equalsIgnoreCase("U")) {
            updatePassword();
        } else {
            return;
        }
    }

    private void searchByService() {
        System.out.print("\nEnter the service name to search: ");
        String servicio = scanner.nextLine();
        conexion.findPasswordsByService(servicio);
    }

    private void addPassword() {
        String servicio;
        while (true) {
            System.out.print("\nEnter service name: ");
            servicio = scanner.nextLine().trim();
            if (!servicio.isEmpty()) {
                break;
            }
            System.out.println("\nError: Service name cannot be empty. Please try again.\n");
        }
        String username;
        while (true) {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            if (!username.isEmpty()) {
                break;
            }
            System.out.println("\nError: Username cannot be empty. Please try again.\n");
        }
        String password;
        while (true) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (!password.isEmpty()) {
                break;
            }
            System.out.println("\nError: Password cannot be empty. Please try again.\n");
        }
        conexion.insertPassword(servicio.toUpperCase(), username, password);
    }

    private void updatePassword() {
        System.out.print("\nEnter the ID of the password to update \nOr press Enter to cancel: ");
        String idInput = scanner.nextLine().trim();
        if (idInput.isEmpty()) {
            System.out.println("Update canceled.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid ID format. Please enter a number.");
            return;
        }
        String servicio;
        while (true) {
            System.out.print("Enter new service name: ");
            servicio = scanner.nextLine().trim();
            if (!servicio.isEmpty()) {
                break;
            }
            System.out.println("\nError: Service name cannot be empty. Please try again.\n");
        }
        String username;
        while (true) {
            System.out.print("Enter new username: ");
            username = scanner.nextLine().trim();
            if (!username.isEmpty()) {
                break;
            }
            System.out.println("\nError: Username cannot be empty. Please try again.\n");
        }
        String password;
        while (true) {
            System.out.print("Enter new password: ");
            password = scanner.nextLine().trim();
            if (!password.isEmpty()) {
                break;
            }
            System.out.println("\nError: Password cannot be empty. Please try again.\n");
        }
        conexion.updatePassword(id, servicio, username, password);
    }

    private void deletePassword() {
        System.out.print("\nEnter the ID of the password to delete\nOr press Enter to cancel: ");
        String option = scanner.nextLine();

        if (option.isEmpty()) {
            System.out.println("\nOperation cancelled.");
            return;
        }

        List<Integer> validIds = conexion.getValidIds();

        try {
            int id = Integer.parseInt(option);

            if (validIds.contains(id)) {
                conexion.deletePassword(id);
            } else {
                System.out.println("\nID not found in the database.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid ID format. Please enter a number.");
        }
    }

}