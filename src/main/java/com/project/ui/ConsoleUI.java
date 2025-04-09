package com.project.ui;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
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
            //Logger.log("Резервное копирование завершено!");
        }));

        panel.addComponent(new Button("Восстановить резервную копию", () -> {
            mainWindow.close();
            closeGUI();
            RestoreManager restoreManager = new RestoreManager();
            restoreManager.startRestore();
            //Logger.log("Восстановление завершено!");
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

        int textBoxWidth = 30;

        panel.addComponent(new Label("Источник для копирования:"));
        TextBox sourceTextBox = new TextBox(new TerminalSize(textBoxWidth, 1))
                .setText(Config.getProperty("source.directory",
                        System.getProperty("user.home") + "/Documents"));
        panel.addComponent(sourceTextBox);

        panel.addComponent(new Label("Каталог для резервных копий:"));
        TextBox backupTextBox = new TextBox(new TerminalSize(textBoxWidth, 1))
                .setText(Config.getProperty("backup.directory",
                        System.getProperty("user.home") + "/backups"));
        panel.addComponent(backupTextBox);

        panel.addComponent(new Label("Каталог для восстановления:"));
        TextBox restoreTextBox = new TextBox(new TerminalSize(textBoxWidth, 1))
                .setText(Config.getProperty("restore.directory",
                        System.getProperty("user.home") + "/restores"));
        panel.addComponent(restoreTextBox);

        panel.addComponent(new Label("Сжимать резервную копию:"));
        CheckBox compressionCheck = new CheckBox();
        compressionCheck.setChecked(Boolean.parseBoolean(Config.getProperty("backup.compression", "false")));
        panel.addComponent(compressionCheck);

        panel.addComponent(new Label("Режим резервного копирования:"));
        RadioBoxList<String> modeSelect = new RadioBoxList<>();
        modeSelect.addItem("full");
        modeSelect.addItem("incremental");
        String currentMode = Config.getProperty("backup.mode", "full");
        modeSelect.setCheckedItem(currentMode);
        panel.addComponent(modeSelect);

        Button saveButton = new Button("Сохранить", () -> {
            String sourcePath = sourceTextBox.getText().trim();
            String backupPath = backupTextBox.getText().trim();
            String restorePath = restoreTextBox.getText().trim();
            String chosenMode = modeSelect.getCheckedItem();
            boolean compressionEnabled = compressionCheck.isChecked();
            StringBuilder message = new StringBuilder();

            // Проверка на нахождение каталога резервной копии внутри источника
            Path srcDir = Paths.get(sourcePath).toAbsolutePath();
            Path bkpDir = Paths.get(backupPath).toAbsolutePath();
            if(bkpDir.startsWith(srcDir)) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Каталог резервной копии не может находиться внутри источника!", MessageDialogButton.OK);
                return;
            }

            boolean validationPassed = true;

            // Проверка и создание директорий, если их нет
            try {
                if (!Files.exists(srcDir)) {
                    Files.createDirectories(srcDir);
                    message.append("Создана директория источника: ").append(sourcePath).append("\n");
                }
            } catch (IOException e) {
                message.append("Ошибка при создании источника (").append(sourcePath)
                        .append("): ").append(e.getMessage()).append("\n");
                validationPassed = false;
            }

            try {
                if (!Files.exists(bkpDir)) {
                    Files.createDirectories(bkpDir);
                    message.append("Создан каталог для резервных копий: ").append(backupPath).append("\n");
                }
            } catch (IOException e) {
                message.append("Ошибка при создании каталога резервных копий (").append(backupPath)
                        .append("): ").append(e.getMessage()).append("\n");
                validationPassed = false;
            }

            try {
                Path rstDir = Paths.get(restorePath);
                if (!Files.exists(rstDir)) {
                    Files.createDirectories(rstDir);
                    message.append("Создан каталог для восстановления: ").append(restorePath).append("\n");
                }
            } catch (IOException e) {
                message.append("Ошибка при создании каталога восстановления (").append(restorePath)
                        .append("): ").append(e.getMessage()).append("\n");
                validationPassed = false;
            }

            // Сохранение в файл конфигурации
            if (validationPassed) {
                Config.setProperty("source.directory", sourcePath);
                Config.setProperty("backup.directory", backupPath);
                Config.setProperty("restore.directory", restorePath);
                Config.setProperty("backup.compression", String.valueOf(compressionEnabled));
                Config.setProperty("backup.mode", chosenMode);
                Config.saveConfig();
                message.append("Настройки успешно сохранены.");
            } else {
                message.append("Настройки не сохранены из-за ошибок валидации.");
            }

            MessageDialog.showMessageDialog(gui, "Результат сохранения", message.toString(), MessageDialogButton.OK);
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
