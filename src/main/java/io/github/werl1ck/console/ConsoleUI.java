package io.github.werl1ck.console;

import io.github.werl1ck.service.GameLauncher;
import io.github.werl1ck.config.LaunchOptions;
import io.github.werl1ck.service.VersionInstall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Stream;

public class ConsoleUI {

    private final Scanner input = new Scanner(System.in);
    private final VersionInstall installer = new VersionInstall();
    private final GameLauncher launcher = new GameLauncher(installer);

    public void ui() throws Exception {
        while (true) {
            System.out.println("""
        ╭──────────────────────────────────────────╮
        │                                          │
        │           MINECRAFT LAUNCHER             │
        │                 v0.5.0                   │
        │                                          │
        ├──────────────────────────────────────────┤
        │                                          │
        │   [1]  Launch game                       │
        │   [2]  Show file directory               │
        │   [3]  Delete game                       │
        │   [4]  Install game                      │
        │   [5]  Info                              │
        │                                          │
        ╰──────────────────────────────────────────╯

        ◄ Ready to launch: 
        """);

            System.out.println();
            String userInput = input.nextLine();

            switch (userInput) {
                case "1" -> gameLauncher();
                case "2" -> showDirectory();
                case "3" -> removeVersion();
                case "4" -> installingGame();
                case "5" -> System.out.println("https://github.com/werl1ck");
                default -> System.out.println("Incorrect input");
            }

            String userChoice = input.nextLine().trim();

            if (userChoice.equals("n") || userChoice.equals("no")) {
                break;
            }
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
        String gameVersion = input.nextLine().trim();

        System.out.println("Enter your nickname: ");
        String nickname = input.nextLine().trim();

        System.out.println("Enter RAM in MB: ");
        int jvmRam = Integer.parseInt(input.nextLine().trim());

        ProcessBuilder builder = new ProcessBuilder(launcher.buildCommand(new LaunchOptions(
                gameVersion,
                nickname,
                jvmRam
        )));

        builder.inheritIO();
        Process process = builder.start();
        System.out.println("Exit code: " + process.waitFor());
    }

    public void removeVersion() throws Exception {
        System.out.println("Enter version for delete: ");
        String userVersionInput = input.nextLine().trim();
        System.out.println("Deleting files from: %s...".formatted(userVersionInput));
        consoleProgressBar();
        Thread.sleep(5000);
        try (Stream<Path> walk = Files.walk(installer.getGameVersionDirectory(userVersionInput))) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            System.out.println("Error: directory is empty");
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            System.out.println("Error");
        }

        System.out.println("Finished");
    }

    public void consoleProgressBar() throws Exception {
        int total = 50;
        for (int i = 0; i <= total; i++) {
            StringBuilder bar = new StringBuilder("[");
            for (int j = 0; j < total; j++) {
                if (j < 1) {
                    bar.append("=");
                } else if (j == i) {
                    bar.append(">");
                } else {
                    bar.append(" ");
                }
            }
            bar.append("] ").append((i * 100) / total).append("%");
            System.out.println("\r" + bar.toString());
            Thread.sleep(100);
        }
        System.out.print("");
    }
}
