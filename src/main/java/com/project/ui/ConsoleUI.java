package com.project.ui;

import com.project.backup.BackupManager;
import com.project.config.Config;
import com.project.restore.RestoreManager;
import com.project.util.Logger;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConsoleUI {
    private Screen screen;
    private MultiWindowTextGUI gui;
    private BasicWindow mainWindow;

    public ConsoleUI() {
        try {
            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
            screen = terminalFactory.createScreen();
            screen.startScreen();
            gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace());
        } catch (IOException e) {
            System.err.println("Ошибка инициализации терминала: " + e.getMessage());
        }
    }

    public void start() {
        mainWindow = new BasicWindow("Утилита резервного копирования");
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        panel.addComponent(new Label("Выберите действие:"));

        panel.addComponent(new Button("Создать резервную копию", () -> {
            mainWindow.close();
            closeGUI();
            BackupManager backupManager = new BackupManager();
            backupManager.startBackup();
            Logger.log("Резервное копирование завершено!");
        }));

        panel.addComponent(new Button("Восстановить резервную копию", () -> {
            mainWindow.close();
            closeGUI();
            RestoreManager restoreManager = new RestoreManager();
            restoreManager.startRestore();
            Logger.log("Восстановление завершено!");
        }));

        panel.addComponent(new Button("Настройки", this::showSettingsWindow));

        panel.addComponent(new Button("Выход", () -> {
            mainWindow.close();
            closeGUI();
            Logger.log("Программа завершена...");
        }));

        mainWindow.setComponent(panel);
        gui.addWindowAndWait(mainWindow);
    }

    private void showSettingsWindow() {
        BasicWindow settingsWindow = new BasicWindow("Настройки резервного копирования");
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        panel.addComponent(new Label("Источник для копирования:"));
        TextBox sourceTextBox = new TextBox().setText(
                Config.getProperty("source.directory", System.getProperty("user.home"))
        );
        panel.addComponent(sourceTextBox);

        panel.addComponent(new Label("Каталог для резервных копий:"));
        TextBox backupTextBox = new TextBox().setText(
                Config.getProperty("backup.directory", System.getProperty("user.home"))
        );
        panel.addComponent(backupTextBox);

        panel.addComponent(new Label("Каталог для восстановления:"));
        TextBox restoreTextBox = new TextBox().setText(
                Config.getProperty("restore.directory", System.getProperty("user.home"))
        );
        panel.addComponent(restoreTextBox);

        Button saveButton = new Button("Сохранить", () -> {
            String sourcePath = sourceTextBox.getText().trim();
            String backupPath = backupTextBox.getText().trim();
            String restorePath = restoreTextBox.getText().trim();

            // Проверка и создание директорий, если их нет
            boolean validationPassed = true;
            try {
                Path srcDir = Paths.get(sourcePath);
                if (!Files.exists(srcDir)) {
                    Files.createDirectories(srcDir);
                    Logger.log("Создана директория источника: " + sourcePath);
                }
            } catch (IOException e) {
                Logger.log("Ошибка при создании источника (" + sourcePath + "): " + e.getMessage());
                validationPassed = false;
            }
            try {
                Path bkpDir = Paths.get(backupPath);
                if (!Files.exists(bkpDir)) {
                    Files.createDirectories(bkpDir);
                    Logger.log("Создан каталог для резервных копий: " + backupPath);
                }
            } catch (IOException e) {
                Logger.log("Ошибка при создании каталога резервных копий (" + backupPath + "): " + e.getMessage());
                validationPassed = false;
            }
            try {
                Path rstDir = Paths.get(restorePath);
                if (!Files.exists(rstDir)) {
                    Files.createDirectories(rstDir);
                    Logger.log("Создан каталог для восстановления: " + restorePath);
                }
            } catch (IOException e) {
                Logger.log("Ошибка при создании каталога восстановления (" + restorePath + "): " + e.getMessage());
                validationPassed = false;
            }

            // Сохранение в файл конфигурации
            if (validationPassed) {
                Config.setProperty("source.directory", sourcePath);
                Config.setProperty("backup.directory", backupPath);
                Config.setProperty("restore.directory", restorePath);
                Config.saveConfig();
                Logger.log("Настройки успешно сохранены.");
            } else {
                Logger.log("Настройки не сохранены из-за ошибок валидации.");
            }
            settingsWindow.close();
        });

        Button cancelButton = new Button("Отмена", settingsWindow::close);
        panel.addComponent(saveButton);
        panel.addComponent(cancelButton);

        settingsWindow.setComponent(panel);
        gui.addWindowAndWait(settingsWindow);
    }

    private void closeGUI() {
        if (screen != null) {
            try {
                screen.stopScreen();
            } catch (IOException e) {
                System.err.println("Ошибка при остановке экрана: " + e.getMessage());
            }
        }
    }
}
