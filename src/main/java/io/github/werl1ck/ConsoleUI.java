package io.github.werl1ck;

import java.util.Scanner;

public class ConsoleUI {

    private final Scanner input = new Scanner(System.in);
    private final VersionInstall installer = new VersionInstall();

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

        System.out.println();
        String userInput = input.nextLine();

        switch (userInput) {
            case "1" -> System.out.println("Starting game...");
            case "2" -> showDirectory();
            case "3" -> System.out.println("...");
            case "4" -> installingGame();
            case "5" -> System.out.println("https://github.com/werl1ck");
            default -> System.out.println("Incorrect input");
        }
    }

    public void showDirectory() {
        System.out.println("Path: %s"
                .formatted(installer.getGameDirectory()));
    }

    public void installingGame() {
        System.out.println("Enter Minecraft version: ");
        String version = input.nextLine().trim();

        System.out.println("Enter your nickname: ");
        String nickname = input.nextLine().trim();

        System.out.println("Enter RAM in MB: ");
        int jvmRam = Integer.parseInt(input.next().trim());

        try {
            installer.install(version);
        } catch (Exception e) {
            System.out.println("Installation failed: %s"
                    .formatted(e.getMessage()));
            e.printStackTrace();
        }
    }
}
