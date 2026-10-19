package io.github.werl1ck;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.versions.VanillaVersion;

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

        updater.update(gameDirectory);

        System.out.println("Installation finished");
    }
}
