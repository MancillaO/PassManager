package negocio;

import java.util.List;
import java.util.Scanner;
import datos.ConexionBD;
import utils.ConsoleUtils;

public class PasswordManager {
    private ConexionBD conexion;
    private ConsoleUtils consoleUtils = new ConsoleUtils();
    private Scanner scanner;

    public PasswordManager(ConexionBD conexion, Scanner scanner) {
        this.conexion = conexion;
        this.scanner = scanner;
    }

    public void mostrarMenuPrincipal(String tipoBD, Menu menuPrincipal) {
        boolean esMongoDB = "mongodb".equalsIgnoreCase(tipoBD);
        consoleUtils.clearScreen();

        while (true) {
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|                       P A S S W O R D                      |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|  1. Add new password                                       |");
            System.out.println("|  2. View all passwords                                     |");
            if (!esMongoDB) {
                System.out.println("|  3. Search password by service                             |");
                System.out.println("|  4. Update password                                        |");
                System.out.println("|  5. Delete password                                        |");
            }
            System.out.println("|                                                            |");
            System.out.println("|  0. Change database                                        |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.print("Select an option: ");
            String option = scanner.nextLine();

            try {
                int opcion = Integer.parseInt(option);
                switch (opcion) {
                    case 1:
                        addPassword();
                        break;
                    case 2:
                        if (esMongoDB) {
                            allPasswords();
                        } else {
                            showPasswords(true);
                        }
                        break;
                    case 3:
                        if (!esMongoDB) {
                            searchByService();
                        } else {
                            consoleUtils.clearScreen();
                            System.out.println("\nInvalid option. Please try again.\n");
                        }
                        break;
                    case 4:
                        if (!esMongoDB) {
                            updatePassword();
                        } else {
                            consoleUtils.clearScreen();
                            System.out.println("\nInvalid option. Please try again.\n");
                        }
                        break;
                    case 5:
                        if (!esMongoDB) {
                            deletePassword();
                        } else {
                            consoleUtils.clearScreen();
                            System.out.println("\nInvalid option. Please try again.\n");
                        }
                        break;
                    case 0:
                        conexion.closeConnection();
                        menuPrincipal.iniciar();
                        return;
                    default:
                        consoleUtils.clearScreen();
                        System.out.println("\nInvalid option. Please try again.\n");
                }
            } catch (NumberFormatException e) {
                consoleUtils.clearScreen();
                System.out.println("\nInvalid input. Please enter a number.\n");
            }
        }
    }

    private void allPasswords() {
        consoleUtils.clearScreen();
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        System.out.println("|                                                            |");
        System.out.println("|                      P A S S W O R D S                     |");
        System.out.println("|                                                            |");
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        System.out.println("|                                                            |");
        conexion.getAllPasswords();
        System.out.println("|                                                            |");
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
    }

    private void showPasswords(boolean ShowAll) {
        if (ShowAll) {
            allPasswords();
        }
        System.out.print(
                "Type (D) to delete a password, (U) to update a password \nOr any other key to return to the main menu: ");
        String option = scanner.nextLine();

        if (option.equalsIgnoreCase("D")) {
            deletePassword();
        } else if (option.equalsIgnoreCase("U")) {
            updatePassword();
        } else {
            consoleUtils.clearScreen();
            return;
        }
    }

    private void searchByService() {
        consoleUtils.clearScreen();
        System.out.print("\nEnter the service name to search: ");
        String servicio = scanner.nextLine();
        System.out.println("\n\n|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        System.out.println("|                                                            |");
        System.out.println("|                      P A S S W O R D S                     |");
        System.out.println("|                                                            |");
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        System.out.println("|                                                            |");
        conexion.findPasswordsByService(servicio);
        System.out.println("|                                                            |");
        System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
        showPasswords(false);
    }

    private void addPassword() {
        consoleUtils.clearScreen();
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
        consoleUtils.clearScreen();
        conexion.insertPassword(servicio, username, password);
    }

    private void updatePassword() {
        boolean validIdInput = false;
        int id = -1;

        while (!validIdInput) {
            allPasswords();
            System.out.print("\nEnter the ID of the password to update \nOr press Enter to cancel: ");
            String idInput = scanner.nextLine().trim();

            if (idInput.isEmpty()) {
                consoleUtils.clearScreen();
                System.out.println("\nUpdate canceled.");
                return;
            }

            try {
                id = Integer.parseInt(idInput);
                List<Integer> validIds = conexion.getValidIds();

                if (validIds.contains(id)) {
                    validIdInput = true;
                } else {
                    consoleUtils.clearScreen();
                    System.out.println("\nID not found in the database!!!");
                    System.out.println("\nPress Enter to try again...");
                    scanner.nextLine();
                }
            } catch (NumberFormatException e) {
                consoleUtils.clearScreen();
                System.out.println("\nInvalid ID format. Please enter a number.");
                System.out.println("Press Enter to try again...");
                scanner.nextLine();
            }
        }

        String servicio;
        while (true) {
            System.out.print("\nEnter new service name: ");
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
        consoleUtils.clearScreen();
        conexion.updatePassword(id, servicio, username, password);
    }

    private void deletePassword() {
        while (true) {
            allPasswords();
            System.out.print("\nEnter the ID of the password to delete\nOr press Enter to cancel: ");
            String option = scanner.nextLine();

            if (option.isEmpty()) {
                consoleUtils.clearScreen();
                System.out.println("\nOperation cancelled.\n");
                return;
            }

            List<Integer> validIds = conexion.getValidIds();
            try {
                int id = Integer.parseInt(option);
                if (validIds.contains(id)) {
                    consoleUtils.clearScreen();
                    conexion.deletePassword(id);
                    return;
                } else {
                    consoleUtils.clearScreen();
                    System.out.println("\nID not found in the database!!!\n");
                    System.out.println("Press Enter to try again...");
                    scanner.nextLine();
                    consoleUtils.clearScreen();
                }
            } catch (NumberFormatException e) {
                consoleUtils.clearScreen();
                System.out.println("\nInvalid ID format. Please enter a number.\n");
                System.out.println("Press Enter to try again...");
                scanner.nextLine();
                consoleUtils.clearScreen();
            }
        }
    }
}
