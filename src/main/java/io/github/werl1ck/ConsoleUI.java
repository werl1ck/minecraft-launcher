package io.github.werl1ck;

import java.util.Scanner;

public class ConsoleUI {
    Scanner input = new Scanner(System.in);

    public void ui() {
        System.out.println("""
                --------------------------------------------------------------
                MINECRAFT LAUNCHER 0.0.1V
                
                HELLO, DEAR USER!
                
                Options:
                1) Launch game
                2) Show file directory
                3) Delete game
                4) Install game
                5) Info
                
                --------------------------------------------------------------
                """);

        String userInput = input.nextLine();

        switch (userInput) {
            case "1" -> System.out.println("Installing game...");
            case "2" -> System.out.println("Open...");
            case "3" -> System.out.println("...");
            case "4" -> {
                System.out.println("Enter Minecraft version: ");
                String version = input.nextLine().trim();
                try {
                    VersionInstall installer = new VersionInstall();
                    installer.install(version);
                } catch (Exception e) {
                    System.out.println("Installation failed: %s"
                            .formatted(e.getMessage()));
                    e.printStackTrace();
                }
            }
            default -> System.out.println("Incorrect input");
        }
    }
}
