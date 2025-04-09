package com.project.config;

import java.io.*;
import java.util.Properties;

public class Config {
    private static Properties properties = new Properties();
    private static String configFilePath = "config.properties";

    public static void loadConfig(String fileName) {
        configFilePath = fileName;
        try (InputStream input = new FileInputStream(configFilePath)) {

            properties.load(input);

        } catch (Exception e) {
            System.err.println("Ошибка при загрузке конфигурации: " + e.getMessage());
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