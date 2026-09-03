package org.example.servinet.config;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigLoad {

    private static Map<String, Object> config;

    public static void loadConfig() {

        Yaml yaml = new Yaml();

        try (InputStream input = ConfigLoad.class
                .getClassLoader()
                .getResourceAsStream("config/config.yml")) {

            if (input == null) {
                throw new RuntimeException("No se encontró config.yml");
            }

            config = yaml.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Error cargando config.yml", e);
        }
    }

    public static String getYmlHost() {
        return getData("database.host", String.class);
    }

    public static int getYmlPort() {
        return getData("database.port", Integer.class);
    }

    public static String getYmlName() {
        return getData("database.name", String.class);
    }

    public static String getYmlUsername() {
        return getData("database.username", String.class);
    }

    public static String getYmlPassword() {
        return getData("database.password", String.class);
    }

    public static <T> T getData(String key, Class<T> type) {

        String[] segments = key.split("\\.");

        Object data = config;

        for (String segment : segments) {

            if (!(data instanceof Map<?, ?> map)) {
                return null;
            }

            data = map.get(segment);

            if (data == null) {
                return null;
            }
        }

        if (!type.isInstance(data)) {
            throw new IllegalArgumentException(
                    "El valor '" + key + "' debe ser de tipo "
                            + type.getSimpleName()
                            + ", pero se encontró "
                            + data.getClass().getSimpleName()
            );
        }

        return type.cast(data);
    }


}
