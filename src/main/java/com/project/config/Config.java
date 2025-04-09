package com.project.config;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();
    private static String configFilePath;

    static {
        try {
            Path jarPath = Paths.get(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            configFilePath = jarPath.resolve("config.properties").toString();
        } catch (URISyntaxException e) {
            System.err.println("Ошибка определения пути к .jar-файлу: " + e.getMessage());
            configFilePath = "config.properties";
        }

        if (Files.exists(Paths.get(configFilePath))) {
            try (InputStream input = new FileInputStream(configFilePath)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Ошибка при загрузке конфигурации: " + e.getMessage());
            }
        } else {
            System.out.println("Файл конфигурации не найден. Будут использованы значения по умолчанию.");
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void saveConfig() {
        try (OutputStream output = new FileOutputStream(configFilePath)) {
            properties.store(output, "BackupTool configuration updated");
        } catch (IOException e) {
            System.err.println("Ошибка сохранения конфигурации: " + e.getMessage());
        }
    }
}
