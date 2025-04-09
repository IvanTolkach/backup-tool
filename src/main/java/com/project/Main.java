package com.project;

import com.project.ui.ConsoleUI;
import com.project.config.Config;

public class Main
{
    public static void main( String[] args ) {

        Config.loadConfig("config.properties");

        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
