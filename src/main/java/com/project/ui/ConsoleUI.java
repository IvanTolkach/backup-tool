package com.project.ui;

import com.project.backup.BackupManager;
import com.project.restore.RestoreManager;
import com.project.util.Logger;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

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
        panel.addComponent(new Button("Выход", () -> {
            mainWindow.close();
            closeGUI();
            Logger.log("Программа завершена...");
        }));

        mainWindow.setComponent(panel);
        gui.addWindowAndWait(mainWindow);
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
