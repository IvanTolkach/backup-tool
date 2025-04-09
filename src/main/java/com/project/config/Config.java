package com.project.config;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Properties properties = new Properties();

    public static void loadConfig(String fileName) {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.out.println("Не удалось найти файл конфигурации " + fileName);
                return;
            }
            properties.load(input);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке конфигурации: " + e.getMessage());
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}