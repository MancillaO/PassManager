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
        if (!dbType.equalsIgnoreCase("mysql") && !dbType.equalsIgnoreCase("postgresql") && !dbType.equalsIgnoreCase("mongodb")) {
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
            System.err.println("Error al cargar el driver JDBC para " + this.dbType);
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
                System.err.println("Error al cerrar la conexión:");
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
                System.out.println("Contraseña insertada correctamente en " + dbType + ". Filas afectadas: " + rows);
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar en " + dbType + ":");
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
            System.out.println("Contraseña insertada correctamente en MongoDB");
        } catch (Exception e) {
            System.err.println("Error al insertar en MongoDB:");
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
    
    // READ para MongoDB - Obtener todas las contraseñas
    private void getAllPasswordsMongo() {
        try {
            MongoCollection<Document> collection = getMongoCollection();
            FindIterable<Document> documents = collection.find();
            
            boolean found = false;
            for (Document doc : documents) {
                found = true;
                System.out.println("ID: " + doc.getObjectId("_id") +
                        ", Servicio: " + doc.getString("servicio") +
                        ", Usuario: " + doc.getString("username") +
                        ", Contraseña: " + doc.getString("password") +
                        ", Fecha: " + doc.getDate("creation_date"));
            }
            
            if (!found) {
                System.out.println("No hay contraseñas almacenadas en MongoDB");
            }
        } catch (Exception e) {
            System.err.println("Error al consultar en MongoDB:");
            e.printStackTrace();
        }
    }

    // READ - Buscar contraseñas por servicio
    public void findPasswordsByService(String servicio) {
        if ("mongodb".equals(dbType)) {
            System.out.println("Búsqueda por servicio no implementada para MongoDB");
            return;
        }
        
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
        if ("mongodb".equals(dbType)) {
            System.out.println("Búsqueda por ID no implementada para MongoDB");
            return;
        }
        
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
        if ("mongodb".equals(dbType)) {
            System.out.println("Actualización no implementada para MongoDB");
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
        if ("mongodb".equals(dbType)) {
            System.out.println("Eliminación no implementada para MongoDB");
            return;
        }
        
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