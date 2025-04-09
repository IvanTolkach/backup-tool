package com.project.backup;

public class BackupTask implements Runnable {
    @Override
    public void run() {
        BackupManager backupManager = new BackupManager();
        backupManager.startBackup();
    }
}