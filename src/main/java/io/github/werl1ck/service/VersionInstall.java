package io.github.werl1ck.service;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersion;
import fr.flowarg.flowupdater.versions.fabric.FabricVersionBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class VersionInstall {
    private final Path gameDirectory = Path.of(
            System.getProperty("user.home"),
            "launcher-minecraft"
    );
    private final Scanner inputChoice = new Scanner(System.in);

    public void install(String version) throws Exception {
        System.out.println("""
                Select option:
                [1] Vanilla
                [2] Fabric (Modded with FabricAPI)
                """);

        String userInput = inputChoice.nextLine().trim().toLowerCase();

        VanillaVersion versionGame =
                new VanillaVersion.VanillaVersionBuilder()
                        .withName(version)
                        .build();

        FlowUpdater updater;

        switch (userInput) {
            case "1" -> {
                updater = new FlowUpdater.FlowUpdaterBuilder()
                        .withVanillaVersion(versionGame)
                        .build();
            }
            case "2" -> {
                FabricVersion fabricVersion = new FabricVersionBuilder()
                        .build();

                updater = new FlowUpdater.FlowUpdaterBuilder()
                        .withVanillaVersion(versionGame)
                        .withModLoaderVersion(fabricVersion)
                        .build();

                Files.createDirectories(getGameVersionDirectory(version).resolve("mods"));
            }
            default -> {
                System.out.println("Incorrect input");
                return;
            }
        }

        System.out.println("Installing Minecraft %s, ...".formatted(version));
        Path pathVersion = gameDirectory.resolve(version);

        updater.update(pathVersion);

        if (Files.exists(pathVersion) && Files.isDirectory(pathVersion))  {
            boolean hasFiles = Files.list(pathVersion).anyMatch(Files::isRegularFile);

            if (hasFiles) {
                System.out.println("Installation finished successfully!");
            } else {
                System.out.println("Version directory is empty...");
            }
        } else {
            System.out.println("Version not downloaded...");
        }
    }

    public Path getGameDirectory() {
        return gameDirectory;
    }

    public Path getGameVersionDirectory(String version) {
        Path pathVersion = gameDirectory.resolve(version);
        return pathVersion;
    }
}
