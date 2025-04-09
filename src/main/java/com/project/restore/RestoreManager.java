package com.project.restore;

import com.project.config.Config;
import com.project.util.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RestoreManager {
    private String restoreDirPath;
    private String backupDirPath;

    public RestoreManager() {
        restoreDirPath = Config.getProperty("restore.directory", System.getProperty("user.home")+ "/restores");
        backupDirPath = Config.getProperty("backup.directory", System.getProperty("user.home")+ "/backups");
    }

    public void startRestore() {
        Logger.log("Начало восстановления резервной копии...");

        Logger.log("Каталог резервных копий: " + backupDirPath);
        Logger.log("Целевой каталог для восстановления: " + restoreDirPath);

        try {
            Path latestBackup = Files.list(Paths.get(backupDirPath))
                    .filter(path -> Files.isDirectory(path) || path.getFileName().toString().endsWith(".zip"))
                    .max((p1, p2) -> Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified()))
                    .orElse(null);
            if (latestBackup == null) {
                Logger.log("Резервных копий не найдено.");
                return;
            }

            Logger.log("Найден резервный объект для восстановления: " + latestBackup.toString());
            Path destination = Paths.get(restoreDirPath);
            Files.createDirectories(destination);

            if (latestBackup.getFileName().toString().endsWith(".zip")) {
                unzip(latestBackup, destination);
                Logger.log("Восстановление из архива завершено. Данные восстановлены в: " + destination.toString());
            } else {
                Files.walk(latestBackup)
                        .forEach(source -> {
                            Path dest = destination.resolve(latestBackup.relativize(source));
                            try {
                                if (Files.isDirectory(source)) {
                                    Files.createDirectories(dest);
                                } else {
                                    Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                Logger.log("Ошибка при восстановлении файла " + source.toString() + ": " + e.getMessage());
                            }
                        });
                Logger.log("Восстановление завершено. Данные восстановлены в: " + destination.toString());
            }
        } catch (IOException e) {
            Logger.log("Ошибка при восстановлении резервной копии: " + e.getMessage());
        }
    }

    private void unzip(Path zipFilePath, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFilePath = destDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    Files.createDirectories(newFilePath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(newFilePath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}
