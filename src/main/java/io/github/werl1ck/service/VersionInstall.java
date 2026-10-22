package io.github.werl1ck.service;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.versions.VanillaVersion;

import java.nio.file.Files;
import java.nio.file.Path;

public class VersionInstall {
    private final Path gameDirectory = Path.of(
            System.getProperty("user.home"),
            "launcher-minecraft"
    );

    public void install(String version) throws Exception {
        VanillaVersion versionGame =
                new VanillaVersion.VanillaVersionBuilder()
                        .withName(version)
                        .build();

        FlowUpdater updater =
                new FlowUpdater.FlowUpdaterBuilder()
                        .withVanillaVersion(versionGame)
                        .build();

        System.out.println("Installing Minecraft %s, ...".formatted(version));

        Path pathVersion = gameDirectory.resolve(version);

        updater.update(pathVersion);

        Path versionJson = pathVersion
                .resolve(version + ".json");

        if (Files.isRegularFile(versionJson)) {
            System.out.println("Installation finished");
        } else {
            System.out.println("Version not a downloaded...");
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
