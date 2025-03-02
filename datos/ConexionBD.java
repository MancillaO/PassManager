package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Importaciones para MongoDB
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import org.bson.Document;

public class ConexionBD {
    private static final String POSTGRESQL_URL = EnvLoader.get("POSTGRESQL_URL");
    private static final String POSTGRESQL_USER = EnvLoader.get("POSTGRESQL_USER");
    private static final String POSTGRESQL_PASSWORD = EnvLoader.get("POSTGRESQL_PASSWORD");

    private static final String MYSQL_URL = EnvLoader.get("MYSQL_URL");
    private static final String MYSQL_USER = EnvLoader.get("MYSQL_USER");
    private static final String MYSQL_PASSWORD = EnvLoader.get("MYSQL_PASSWORD");

    private static final String MONGODB_URI = EnvLoader.get("MONGODB_URI");
    private static final String MONGODB_DATABASE = EnvLoader.get("MONGODB_DB");
    private static final String MONGODB_COLLECTION = EnvLoader.get("MONGODB_COLLECTION");

    private String dbType;
    private Connection activeConnection;
    private MongoClient mongoClient;

    public ConexionBD(String dbType) {
        if (!dbType.equalsIgnoreCase("mysql") && !dbType.equalsIgnoreCase("postgresql")
                && !dbType.equalsIgnoreCase("mongodb")) {
            throw new IllegalArgumentException(
                    "Tipo de base de datos no soportado: solo se admite 'mysql', 'postgresql' o 'mongodb'");
        }
        this.dbType = dbType.toLowerCase();
        try {
            if ("mysql".equals(this.dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if ("postgresql".equals(this.dbType)) {
                Class.forName("org.postgresql.Driver");
            }
            // MongoDB no requiere cargar un driver JDBC
        } catch (ClassNotFoundException e) {
            System.err.println("\nError al cargar el driver JDBC para " + this.dbType);
            e.printStackTrace();
        }
    }

    // Método para obtener la conexión activa o crear una nueva si es necesario
    public Connection getConnection() throws SQLException {
        if ("mongodb".equals(dbType)) {
            throw new SQLException("Para MongoDB, use getMongoCollection() en lugar de getConnection()");
        }

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

    // Método para obtener la colección de MongoDB
    public MongoCollection<Document> getMongoCollection() {
        if (!"mongodb".equals(dbType)) {
            throw new IllegalStateException("Este método solo se puede usar con MongoDB");
        }

        if (mongoClient == null) {
            mongoClient = MongoClients.create(MONGODB_URI);
        }

        MongoDatabase database = mongoClient.getDatabase(MONGODB_DATABASE);
        return database.getCollection(MONGODB_COLLECTION);
    }

    // Método para cerrar la conexión activa
    public void closeConnection() {
        if ("mongodb".equals(dbType)) {
            if (mongoClient != null) {
                mongoClient.close();
                mongoClient = null;
            }
            return;
        }

        if (activeConnection != null) {
            try {
                if (!activeConnection.isClosed()) {
                    activeConnection.close();
                }
            } catch (SQLException e) {
                System.err.println("\nError al cerrar la conexión:");
                e.printStackTrace();
            } finally {
                activeConnection = null;
            }
        }
    }

    // CREATE - Insertar nueva contraseña
    public void insertPassword(String servicio, String username, String password) {
        if ("mongodb".equals(dbType)) {
            insertPasswordMongo(servicio, username, password);
            return;
        }

        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO passwords (servicio, username, password) VALUES (?, ?, ?)")) {
                stmt.setString(1, servicio);
                stmt.setString(2, username);
                stmt.setString(3, password);
                int rows = stmt.executeUpdate();
                System.out.println("\nPassword inserted successfully in " + dbType);
            }
        } catch (SQLException e) {
            System.err.println("\nError inserting into " + dbType + ":");
            e.printStackTrace();
        }
    }

    // CREATE para MongoDB - Insertar nueva contraseña
    private void insertPasswordMongo(String servicio, String username, String password) {
        try {
            MongoCollection<Document> collection = getMongoCollection();

            Document doc = new Document("servicio", servicio)
                    .append("username", username)
                    .append("password", password)
                    .append("creation_date", new Date());

            collection.insertOne(doc);
            System.out.println("\nPassword inserted successfully into MongoDB");
        } catch (Exception e) {
            System.err.println("\nError inserting into MongoDB:");
            e.printStackTrace();
        }
    }

    // READ - Obtener todas las contraseñas
    public void getAllPasswords() {
        if ("mongodb".equals(dbType)) {
            getAllPasswordsMongo();
            return;
        }
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM passwords ORDER BY id asc")) {
                boolean found = false;

                // Definir los anchos de las columnas
                String format = "| %-2s | %-10s | %-15s | %-22s |\n";

                System.err.printf(format, "ID", "Service", "Username", "Password");
                System.out.println("|                                                            |");
                while (rs.next()) {
                    found = true;
                    System.out.printf(format,
                            rs.getInt("id"),
                            rs.getString("servicio"),
                            rs.getString("username"),
                            rs.getString("password"));
                }
                if (!found) {
                    System.out.println("| No passwords stored                                        |");
                }
            }
        } catch (SQLException e) {
            System.out.println("| Error querying in " + dbType + " |");
            e.printStackTrace();
        }
    }

    // READ para MongoDB - Obtener todas las contraseñas
    private void getAllPasswordsMongo() {
        try {
            MongoCollection<Document> collection = getMongoCollection();
            FindIterable<Document> documents = collection.find();

            boolean found = false;

            // Definir formato con anchos fijos para cada columna
            String format = "| %-2s | %-10s | %-15s | %-22s |\n";

            // Imprimir encabezado
            System.err.printf(format, "ID", "Service", "Username", "Password");
            System.out.println("|                                                            |");

            for (Document doc : documents) {
                found = true;
                System.out.printf(format,
                        doc.getObjectId("_id").toString().substring(0, 8),
                        doc.getString("servicio"),
                        doc.getString("username"),
                        doc.getString("password"));
            }

            if (!found) {
                System.out.println("| No passwords stored                                        |");
            }

        } catch (Exception e) {
            System.out.println("| Error querying in MongoDB |");
            e.printStackTrace();
        }
    }

    // READ - Buscar contraseñas por servicio
    public void findPasswordsByService(String servicio) {
        if ("mongodb".equals(dbType)) {
            System.out.println("Service-based search not implemented for MongoDB");
            return;
        }

        try (Connection conn = getConnection()) {
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
                    System.out.println("No results found for '" + servicio + "' in " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("\nError searching by service in " + dbType + ":");
            e.printStackTrace();
        }
    }

    // UPDATE - Actualizar contraseña existente
    public void updatePassword(int id, String nuevoServicio, String nuevoUsername, String nuevaPassword) {
        if ("mongodb".equals(dbType)) {
            System.out.println("Update not implemented for MongoDB");
            return;
        }

        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE passwords SET servicio = ?, username = ?, password = ? WHERE id = ?")) {
                stmt.setString(1, nuevoServicio);
                stmt.setString(2, nuevoUsername);
                stmt.setString(3, nuevaPassword);
                stmt.setInt(4, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Password updated successfully in " + dbType);
                } else {
                    System.out.println("No password found with ID " + id + " in " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("\nError updating in " + dbType + ":");
            e.printStackTrace();
        }
    }

    // DELETE - Eliminar contraseña
    public void deletePassword(int id) {
        if ("mongodb".equals(dbType)) {
            System.out.println("Deletion not implemented for MongoDB");
            return;
        }

        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM passwords WHERE id = ?")) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("\nPassword successfully deleted from " + dbType);
                } else {
                    System.out.println("No password found with ID " + id + " in " + dbType);
                }
            }
        } catch (SQLException e) {
            System.err.println("\nError deleting from " + dbType + ":");
            e.printStackTrace();
        }
    }

    public List<Integer> getValidIds() {
        if ("mongodb".equals(dbType)) {
            System.out.println("Function not implemented for MongoDB");
            return new ArrayList<>();
        }
        List<Integer> idList = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id FROM passwords ORDER BY id;")) {
            while (rs.next()) {
                idList.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("Error querying the IDs in " + dbType);
            e.printStackTrace();
        }
        return idList;
    }
}
