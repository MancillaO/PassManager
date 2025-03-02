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
            mostrarMenuPrincipal();
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
            System.out.println("|  3. MongoDB (Not available)                                |");
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
                        System.err.println("\nMongoDB is not currently available.");
                        break;
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

    private void mostrarMenuPrincipal() {
        int opcion = 0;

        while (opcion != 7) {
            System.out.println("\n|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|                      PASSWORD MANAGER                      |");
            System.out.println("|                                                            |");
            System.out.println("|~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|");
            System.out.println("|                                                            |");
            System.out.println("|  1. View all passwords                                     |");
            System.out.println("|  2. Search password by service                             |");
            System.out.println("|  4. Add new password                                       |");
            System.out.println("|  5. Update password                                        |");
            System.out.println("|  6. Delete password                                        |");
            System.out.println("|  7. Exit                                                   |");
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
                        // searchByService();
                        break;
                    case 3:
                        // searchById();
                        break;
                    case 4:
                        // addNewPassword();
                        break;
                    case 5:
                        // updatePassword();
                        break;
                    case 6:
                        // deletePassword();
                        break;
                    case 7:
                        System.out.println("\nExiting...");
                        return;
                    default:
                        System.out.println("\nInvalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input. Please enter a number.");
            }
        }
    }
}