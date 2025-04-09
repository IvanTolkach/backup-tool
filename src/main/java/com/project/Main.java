package com.project;

import com.project.ui.ConsoleUI;

public class Main
{
    public static void main( String[] args ) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {}));

        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
