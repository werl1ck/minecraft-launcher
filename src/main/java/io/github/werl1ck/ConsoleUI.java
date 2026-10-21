package io.github.werl1ck;

import java.util.Scanner;

public class ConsoleUI {

    private final Scanner input = new Scanner(System.in);
    private final VersionInstall installer = new VersionInstall();
    private final GameLauncher launcher = new GameLauncher(installer);
    private final Scanner inputForLaunch = new Scanner(System.in);

    public void ui() throws Exception {
        System.out.println("""
                --------------------------------------------------------------
                MINECRAFT LAUNCHER 0.0.5V
                
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
            case "1" -> gameLauncher();
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

        try {
            installer.install(version);
        } catch (Exception e) {
            System.out.println("Installation failed: %s"
                    .formatted(e.getMessage()));
            e.printStackTrace();
        }
    }

    public void gameLauncher() throws Exception {

        System.out.println("Enter version: ");
        String gameVersion = inputForLaunch.nextLine().trim();

        System.out.println("Enter your nickname: ");
        String nickname = inputForLaunch.nextLine().trim();

        System.out.println("Enter RAM in MB: ");
        int jvmRam = Integer.parseInt(inputForLaunch.nextLine().trim());

        ProcessBuilder builder = new ProcessBuilder(launcher.buildCommand(new LaunchOptions(
                gameVersion,
                nickname,
                jvmRam
        )));

        builder.inheritIO();
        Process process = builder.start();
        System.out.println("Exit code: " + process.waitFor());
    }
}
