package com.project.restore;

import com.project.config.Config;
import com.project.util.Logger;

import java.io.IOException;
import java.nio.file.*;

public class RestoreManager {
    private String restoreDirPath;
    private String backupDirPath;

    public RestoreManager() {
        restoreDirPath = Config.getProperty("restore.directory", System.getProperty("user.home"));
        backupDirPath = Config.getProperty("backup.directory", System.getProperty("user.home"));
    }

    public void startRestore() {
        Logger.log("Начало восстановления резервной копии...");

        try {
            Path latestBackup = Files.list(Paths.get(backupDirPath))
                    .filter(Files::isDirectory)
                    .max((path1, path2) -> Long.compare(path1.toFile().lastModified(), path2.toFile().lastModified()))
                    .orElse(null);
            if (latestBackup == null) {
                Logger.log("Резервных копий не найдено.");
                return;
            }

            Path destination = Paths.get(restoreDirPath);
            Files.createDirectories(destination);
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
        } catch (IOException e) {
            Logger.log("Ошибка при восстановлении резервной копии: " + e.getMessage());
        }
    }
}
