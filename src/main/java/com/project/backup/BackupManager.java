package com.project.backup;

import com.project.config.Config;
import com.project.util.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.*;

public class BackupManager {
    private String backupDirPath;
    private String sourceDirPath;
    private String backupMode;
    private boolean useCompression;

    public BackupManager() {
        backupDirPath = Config.getProperty("backup.directory", System.getProperty("user.home") + "/backups");
        sourceDirPath = Config.getProperty("source.directory", System.getProperty("user.home") + "/Documents");
        backupMode = Config.getProperty("backup.mode", "full");
        useCompression = Boolean.parseBoolean(Config.getProperty("backup.compression", "false"));
    }

    public void startBackup() {
        Logger.log("Начало процесса резервного копирования...");

        Logger.log("Исходная директория для резервного копирования: " + sourceDirPath);
        Logger.log("Целевой каталог для архивов: " + backupDirPath);
        Logger.log("Режим резервного копирования: " + backupMode);
        Logger.log("Сжатие резервной копии: " + (useCompression ? "включено" : "выключено"));

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String backupFolderName = "backup_" + now.format(formatter);

        Path sourceDir = Paths.get(sourceDirPath);
        Path targetDir = Paths.get(backupDirPath, backupFolderName);

        try {
            Files.createDirectories(targetDir);

            long tempLastBackupTimestamp = 0;
            if ("incremental".equalsIgnoreCase(backupMode)) {
                try {
                    tempLastBackupTimestamp = Long.parseLong(Config.getProperty("last.backup.timestamp", "0"));
                    if (tempLastBackupTimestamp != 0) {
                        Logger.log("Инкрементальный режим. Будут резервироваться только файлы, измененные после: " + tempLastBackupTimestamp);
                    }
                } catch (NumberFormatException e) {
                    Logger.log("Некорректное значение last.backup.timestamp. Выполняется полное резервное копирование.");
                }
            }
            final long lastBackupFinal = tempLastBackupTimestamp;

            Files.walk(sourceDir)
                    .forEach(source -> {
                        if (source.toAbsolutePath().startsWith(targetDir.toAbsolutePath()))
                            return;

                        if ("incremental".equalsIgnoreCase(backupMode) && Files.isRegularFile(source)) {
                            try {
                                long lastModified = Files.getLastModifiedTime(source).toMillis();
                                if (lastModified <= lastBackupFinal) {
                                    return;
                                }
                            } catch (IOException e) {
                                Logger.log("Ошибка при проверке времени изменения файла: " + source + ": " + e.getMessage());
                            }
                        }
                        try {
                            Path relativePath = sourceDir.relativize(source);
                            Path destination = targetDir.resolve(relativePath);
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            Logger.log("Ошибка при копировании файла: " + source.toString() + " - " + e.getMessage());
                        }
                    });
            Logger.log("Резервное копирование завершено. Каталог: " + targetDir.toString());

            Config.setProperty("last.backup.timestamp", String.valueOf(System.currentTimeMillis()));
            Config.saveConfig();

            if (useCompression) {
                Path zipFile = Paths.get(targetDir.toString() + ".zip");
                Logger.log("Создается архив резервной копии: " + zipFile);
                zipDirectory(targetDir, zipFile);
                Logger.log("Архив создан: " + zipFile.toString());

                deleteDirectoryRecursively(targetDir);
            }
        } catch (IOException e) {
            Logger.log("Ошибка при создании резервной копии: " + e.getMessage());
        }
    }

    private void zipDirectory(Path sourceDir, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            Logger.log("Ошибка при записи файла в архив: " + path + ": " + e.getMessage());
                        }
                    });
        }
    }

    private void deleteDirectoryRecursively(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
