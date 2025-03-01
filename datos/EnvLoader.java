package datos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> envMap = new HashMap<>();

    static {
        loadEnv();
    }

    private static void loadEnv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        envMap.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo .env: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return envMap.get(key);
    }
}