package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBD {
    private static final String POSTGRESQL_URL = EnvLoader.get("POSTGRESQL_URL");
    private static final String POSTGRESQL_USER = EnvLoader.get("POSTGRESQL_USER");
    private static final String POSTGRESQL_PASSWORD = EnvLoader.get("POSTGRESQL_PASSWORD");

    private static final String MYSQL_URL = EnvLoader.get("MYSQL_URL");
    private static final String MYSQL_USER = EnvLoader.get("MYSQL_USER");
    private static final String MYSQL_PASSWORD = EnvLoader.get("MYSQL_PASSWORD");

    private String dbType;
    private Connection activeConnection;

    // Constructor que establece el tipo de base de datos
    public ConexionBD(String dbType) {
        if (!dbType.equalsIgnoreCase("mysql") && !dbType.equalsIgnoreCase("postgresql")) {
            throw new IllegalArgumentException(
                    "Tipo de base de datos no soportado: solo se admite 'mysql' o 'postgresql'");
        }
        this.dbType = dbType.toLowerCase(); // Normalizar a minúsculas para evitar problemas de comparación

        // Verificar que podemos cargar el driver correspondiente
        try {
            if ("mysql".equals(this.dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if ("postgresql".equals(this.dbType)) {
                Class.forName("org.postgresql.Driver");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver JDBC para " + this.dbType);
            e.printStackTrace();
        }
    }

    // Método para obtener la conexión activa o crear una nueva si es necesario
    public Connection getConnection() throws SQLException {
        if (activeConnection == null || activeConnection.isClosed()) {
            if ("mysql".equals(dbType)) {
                activeConnection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            } else if ("postgresql".equals(dbType)) {
                activeConnection = DriverManager.getConnection(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);
            } else {
                throw new SQLException("Tipo de base de datos no válido: " + dbType);
            }
        }
        return activeConnection;
    }

    // Método para cerrar la conexión activa
    public void closeConnection() {
        if (activeConnection != null) {
            try {
                if (!activeConnection.isClosed()) {
                    activeConnection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión:");
                e.printStackTrace();
            } finally {
                activeConnection = null;
            }
        }
    }

    // CREATE - Insertar nueva contraseña
    public void insertPassword(String servicio, String username, String password) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO passwords (servicio, username, password) VALUES (?, ?, ?)")) {
                stmt.setString(1, servicio);
                stmt.setString(2, username);
                stmt.setString(3, password);
                int rows = stmt.executeUpdate();
                System.out.println("Contraseña insertada correctamente en " + dbType + ". Filas afectadas: " + rows);
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar en " + dbType + ":");
            e.printStackTrace();
        }
    }

    // READ - Obtener todas las contraseñas
    public void getAllPasswords() {
        try (Connection conn = getConnection()) {

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM passwords")) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getInt("id") +
                            ", Servicio: " + rs.getString("servicio") +
                            ", Usuario: " + rs.getString("username") +
                            ", Contraseña: " + rs.getString("password") +
                            ", Fecha: " + rs.getTimestamp("creation_date"));
                }
                if (!found) {
                    System.out.println("No hay contraseñas almacenadas en " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar en " + dbType + ":");
            e.printStackTrace();
        }
    }

    // READ - Buscar contraseñas por servicio
    public void findPasswordsByService(String servicio) {
        try (Connection conn = getConnection()) {
            System.out.println("Resultados en " + dbType + " para servicio '" + servicio + "':");
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM passwords WHERE servicio LIKE ?")) {
                stmt.setString(1, "%" + servicio + "%");
                ResultSet rs = stmt.executeQuery();
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("ID: " + rs.getInt("id") +
                            ", Servicio: " + rs.getString("servicio") +
                            ", Usuario: " + rs.getString("username") +
                            ", Contraseña: " + rs.getString("password"));
                }
                if (!found) {
                    System.out.println("No se encontraron resultados para '" + servicio + "' en " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por servicio en " + dbType + ":");
            e.printStackTrace();
        }
    }

    // READ - Obtener contraseña por ID
    public void getPasswordById(int id) {
        try (Connection conn = getConnection()) {
            System.out.println("Resultado en " + dbType + " para ID " + id + ":");
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM passwords WHERE id = ?")) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") +
                            ", Servicio: " + rs.getString("servicio") +
                            ", Usuario: " + rs.getString("username") +
                            ", Contraseña: " + rs.getString("password"));
                } else {
                    System.out.println("No se encontró ninguna contraseña con ID " + id + " en " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar por ID en " + dbType + ":");
            e.printStackTrace();
        }
    }

    // UPDATE - Actualizar contraseña existente
    public void updatePassword(int id, String nuevoServicio, String nuevoUsername, String nuevaPassword) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE passwords SET servicio = ?, username = ?, password = ? WHERE id = ?")) {
                stmt.setString(1, nuevoServicio);
                stmt.setString(2, nuevoUsername);
                stmt.setString(3, nuevaPassword);
                stmt.setInt(4, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Contraseña actualizada correctamente en " + dbType);
                } else {
                    System.out.println("No se encontró ninguna contraseña con ID " + id + " en " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al actualizar en " + dbType + ":");
            e.printStackTrace();
        }
    }

    // DELETE - Eliminar contraseña
    public void deletePassword(int id) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM passwords WHERE id = ?")) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Contraseña eliminada correctamente de " + dbType);
                } else {
                    System.out.println("No se encontró ninguna contraseña con ID " + id + " en " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar en " + dbType + ":");
            e.printStackTrace();
        }
    }
}