package io.github.werl1ck.console;

import io.github.werl1ck.discord.DiscordRPC;
import io.github.werl1ck.service.GameLauncher;
import io.github.werl1ck.config.LaunchOptions;
import io.github.werl1ck.service.VersionInstall;

import java.awt.*;
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

    private DiscordRPC discord;

    public ConsoleUI(DiscordRPC discord) {
        this.discord = discord;
    }

    public void ui() throws Exception {
        while (true) {


            String pink = ConsoleColors.PINK;
            String reset = ConsoleColors.RESET;

            System.out.println("""
        %s╭──────────────────────────────────────────╮
        │                                          │
        │           MINECRAFT LAUNCHER             │
        │                 %sv1.0.0%s                   │
        │                                          │
        ├──────────────────────────────────────────┤
        │                                          │
        │   %s[1]  Launch game%s                       %s│
        │   %s[2]  Show file directory%s               %s│
        │   %s[3]  Delete game%s                       %s│
        │   %s[4]  Install game%s                      %s│
        │   %s[5]  Info%s                              %s│
        │                                          │
        ╰──────────────────────────────────────────╯
        %s
        ◄ Ready to launch:
        """.formatted(
                    pink,
                    ConsoleColors.WHITE, pink,
                    ConsoleColors.LIME, reset, pink,
                    ConsoleColors.WHITE, reset, pink,
                    ConsoleColors.RED, reset, pink,
                    ConsoleColors.LIME, reset, pink,
                    ConsoleColors.BRIGHT_YELLOW, reset, pink,
                    reset
            ));



            String userInput = input.nextLine();

            switch (userInput) {
                case "1" -> gameLauncher();
                case "2" -> showDirectory();
                case "3" -> removeVersion();
                case "4" -> installingGame();
                case "5" -> System.out.println("""
                        If you liked my work, could you please give it a star on GitHub?
                        
                        https://github.com/werl1ck
                        """);
                default -> System.out.println("Incorrect input");
            }

            System.out.println("Continue? [y/N]");
            String userChoice = input.nextLine().toLowerCase().trim();
            if (userChoice.equals("n")) {
                break;
            }
        }
    }


    public void showDirectory() {
        System.out.println("Path: %s"
                .formatted(installer.getGameDirectory()));

        try {
            if (!Desktop.isDesktopSupported()) {
                System.out.println("Desktop is not supported");
                return;
            }

            Desktop.getDesktop().open(installer.getGameDirectory().toFile());
        } catch (IOException e) {
            System.out.println("Failed to open directory: %s".formatted(e.getMessage()));
            e.printStackTrace();
        }
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

        try {
            System.out.println("Enter version: ");
            String gameVersion = input.nextLine().trim();

            System.out.println("Enter your nickname: ");
            String nickname = input.nextLine().trim();

            System.out.println("Enter RAM in MB: ");
            int jvmRam = Integer.parseInt(input.nextLine().trim());

            System.out.println("Enter game type (vanilla or fabric) : ");
            String gameType = input.nextLine().trim().toLowerCase();

            ProcessBuilder builder = new ProcessBuilder(launcher.buildCommand(new LaunchOptions(
                    gameVersion,
                    nickname,
                    jvmRam,
                    gameType
            )));

            builder.inheritIO();
            Process process = builder.start();
            discord.updateStatus(
                    "Plays Minecraft",
                    "nickname: %s | version: %s"
                            .formatted(nickname, gameVersion)
            );

            System.out.println("Exit code: " + process.waitFor());

            discord.updateStatus(
                    "In the console",
                    "Waiting for your next action..."
            );

        } catch (Exception e) {
            System.out.println("An error occurred: %s".formatted(e.getMessage()));
            e.printStackTrace();
        }
    }

    public void removeVersion() throws Exception {
        System.out.println("Enter version for delete: ");
        String userVersionInput = input.nextLine().trim();
        System.out.println("Are you sure? [y/N]");
        String userChoice = input.nextLine().toLowerCase().trim();
        if (userChoice.equals("n")) {
            System.out.println("Deletion rejected");
        } else if (userChoice.equals("y")) {
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
        } else {
            System.out.println("Incorrect input");
        }
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
            System.out.print("\r" + bar.toString());
            Thread.sleep(100);
        }
        System.out.println("");
    }
}
