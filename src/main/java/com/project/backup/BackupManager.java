package com.project.backup;

import com.project.config.Config;
import com.project.util.Logger;

import java.io.IOException;
import java.nio.file.*;

public class BackupManager {
    private String backupDir;
    private String sourceDirPath;

    public BackupManager() {
        // Чтение пути к каталогу для резервного копирования из конфигурации
        backupDir = Config.getProperty("backup.directory", "/tmp/backup");
        // Чтение исходной директории для копирования из конфигурации
        sourceDirPath = Config.getProperty("source.directory", System.getProperty("user.home"));
    }

    public void startBackup() {
        Logger.log("Начало процесса резервного копирования...");
        // Используем каталог из config.properties
        Path sourceDir = Paths.get(sourceDirPath);
        Path targetDir = Paths.get(backupDir, "backup_" + System.currentTimeMillis());

        try {
            Files.createDirectories(targetDir);
            Files.walk(sourceDir)
                    .forEach(source -> {
                        Path destination = targetDir.resolve(sourceDir.relativize(source));
                        try {
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            Logger.log("Ошибка при копировании файла: " + source.toString() + " " + e.getMessage());
                        }
                    });
            Logger.log("Резервное копирование завершено. Архив создан в: " + targetDir.toString());
        } catch (IOException e) {
            Logger.log("Ошибка создания резервной копии: " + e.getMessage());
        }
    }
}
