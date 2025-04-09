package com.project.backup;

import com.project.config.Config;
import com.project.util.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager {
    private String backupDirPath;
    private String sourceDirPath;

    public BackupManager() {
        backupDirPath = Config.getProperty("backup.directory", System.getProperty("user.home"));
        sourceDirPath = Config.getProperty("source.directory", System.getProperty("user.home"));
    }

    public void startBackup() {
        Logger.log("Начало процесса резервного копирования...");

        Logger.log("Исходная директория для резервного копирования: " + sourceDirPath);
        Logger.log("Целевой каталог для архивов (если параметр отсутствует, используется домашняя директория): " + backupDirPath);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String backupFolderName = "backup_" + now.format(formatter);

        Path sourceDir = Paths.get(sourceDirPath);
        Path targetDir = Paths.get(backupDirPath, backupFolderName);

        try {
            Files.createDirectories(targetDir);
            Files.walk(sourceDir)
                    .forEach(source -> {
                        Path relativePath = sourceDir.relativize(source);
                        Path destination = targetDir.resolve(relativePath);

                        if (source.toAbsolutePath().startsWith(targetDir.toAbsolutePath())) {
                            return;
                        }

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
            Logger.log("Резервное копирование завершено.");
            Logger.log("Создан каталог резервной копии: " + targetDir.toString());
        } catch (IOException e) {
            Logger.log("Ошибка создания резервной копии: " + e.getMessage());
        }
    }
}
