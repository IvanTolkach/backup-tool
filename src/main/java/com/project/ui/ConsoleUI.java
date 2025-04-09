package com.project.ui;

import com.project.backup.BackupManager;
import com.project.restore.RestoreManager;
import com.project.util.Logger;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;

public class ConsoleUI {
    private Screen screen;
    private MultiWindowTextGUI gui;

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
        final Window window = new BasicWindow("Утилита резервного копирования");
        Panel panel = new Panel(new GridLayout(1));

        panel.addComponent(new Label("Выберите действие:"));
        panel.addComponent(new Button("Создать резервную копию", () -> {
            window.close();
            BackupManager backupManager = new BackupManager();
            backupManager.startBackup();
            System.out.println("Резервное копирование завершено!");
        }));
        panel.addComponent(new Button("Восстановить резервную копию", () -> {
            window.close();
            RestoreManager restoreManager = new RestoreManager();
            restoreManager.startRestore();
            System.out.println("Восстановление завершено!");
        }));
        panel.addComponent(new Button("Выход", window::close));

        window.setComponent(panel);
        gui.addWindowAndWait(window);

        try {
            screen.stopScreen();
        } catch (IOException e) {
            Logger.log("Ошибка остановки экрана: " + e.getMessage());
        }
    }

    private void showMessage(String message) {
        MessageDialog.showMessageDialog(gui, "Сообщение", message, MessageDialogButton.OK);
    }
}
