package restfulbooker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {

    private static final Properties PROPERTIES = load();

    private ConfigLoader() {
    }

    public static String baseUrl() {
        return get("BASE_URL", "base.url");
    }

    public static String username() {
        return get("AUTH_USERNAME", "auth.username");
    }

    public static String password() {
        return get("AUTH_PASSWORD", "auth.password");
    }

    private static String get(String envKey, String propertyKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        String propertyValue = PROPERTIES.getProperty(propertyKey);
        if (propertyValue == null || propertyValue.isBlank()) {
            throw new IllegalStateException(
                    "Missing config value: set env var " + envKey + " or property '" + propertyKey + "' in config.properties");
        }
        return propertyValue;
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
        return properties;
    }
}
