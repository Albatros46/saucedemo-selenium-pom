package com.servet.saucedemo.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * config.properties'i okur. Aynı isimde bir System property varsa (-Dbrowser=firefox) o kazanır.
 * Böylece kod değiştirmeden, sadece komut satırından tarayıcı veya ortam değiştirilebilir.
 */
public final class Config {

    private static final Properties PROPERTIES = load();

    private Config() {
    }

    public static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Ayar bulunamadı: " + key);
        }
        return value.trim();
    }

    public static String baseUrl() {
        return get("baseUrl");
    }

    public static String browser() {
        return get("browser");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static int explicitWaitSeconds() {
        return Integer.parseInt(get("explicitWaitSeconds"));
    }

    public static int pageLoadTimeoutSeconds() {
        return Integer.parseInt(get("pageLoadTimeoutSeconds"));
    }

    private static Properties load() {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties bulunamadı");
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
