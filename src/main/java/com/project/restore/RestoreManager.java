package com.project.restore;

import com.project.util.Logger;

import java.io.IOException;
import java.nio.file.*;

public class RestoreManager {
    // В реальном проекте следует реализовать выбор конкретной резервной копии,
    // возможно, отображать список доступных копий для выбора.
    private String backupDir = "/tmp/backup"; // Можно получить из Config

    public void startRestore() {
        Logger.log("Начало восстановления резервной копии...");
        // Пример: восстанавливаем последнюю резервную копию в домашнюю директорию
        // Здесь используется упрощённая логика; в реальной утилите — поиск нужного архива
        try {
            Path latestBackup = Files.list(Paths.get(backupDir))
                    .filter(Files::isDirectory)
                    .max((path1, path2) -> Long.compare(path1.toFile().lastModified(), path2.toFile().lastModified()))
                    .orElse(null);
            if (latestBackup == null) {
                Logger.log("Резервных копий не найдено.");
                return;
            }
            Path destination = Paths.get(System.getProperty("user.home"), "restored_backup");
            Files.createDirectories(destination);
            Files.walk(latestBackup)
                    .forEach(source -> {
                        Path dest = destination.resolve(latestBackup.relativize(source));
                        try {
                            if (Files.isDirectory(source))
                                Files.createDirectories(dest);
                            else
                                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
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