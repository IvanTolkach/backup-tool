package com.project;

import com.project.ui.ConsoleUI;
import com.project.config.Config;
import com.project.util.Logger;
/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args ) {

        Config.loadConfig("config.properties");

        Logger.init();

        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
