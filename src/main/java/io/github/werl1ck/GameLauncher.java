package io.github.werl1ck;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameLauncher {

    private final VersionInstall versionInstall;

    private final Path nativeFiles = Path.of("natives");

    public GameLauncher(VersionInstall versionInstall) {
        this.versionInstall = versionInstall;
    }

    public String buildRamArgument(LaunchOptions options) {
        var ram = options.getJvmRam();
        String jvmRam;

        if (ram <= 0) {
            throw new IllegalArgumentException("The RAM value must be positive");
        }

        if (ram % 1024 == 0) {
            var correctRam = ram / 1024;
            jvmRam = "-Xmx%sG".formatted(correctRam);
        } else {
            throw new IllegalArgumentException("Your RAM value is not a multiple of 1024");
        }

        return jvmRam;
    }

    public String[] buildJvmArguments(LaunchOptions options) {
        return new String[]{
                "-Djava.library.path=" + versionInstall.getGameDirectory().resolve(nativeFiles),
                buildRamArgument(options)
        };
    }

    public String[] buildGameArguments(LaunchOptions options) {
        String gameDir = versionInstall.getGameDirectory().toAbsolutePath().toString();
        String assetsDir = versionInstall.getGameDirectory().resolve("assets").toAbsolutePath().toString();

        return new String[]{
                "--username", options.getNickname(),
                "--version", options.getGameVersion(),
                "--gameDir", gameDir,
                "--assetsDir", assetsDir,
                "--assetIndex", "1.12",
                "--uuid", "00000000-0000-0000-0000-000000000000",
                "--accessToken", "0",
                "--userType", "legacy",
                "--versionType", "release"

        };
    }

    public String buildClasspath() {
        var pathToClientJar = versionInstall.getGameDirectory().resolve("client.jar").toString();
        var librariesDir = versionInstall.getGameDirectory().resolve("libraries");

        try (Stream<Path> walk = Files.walk(librariesDir)) {
            List<String> jars = walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .map(Path::toString)
                    .collect(Collectors.toList());

            jars.add(0, pathToClientJar);

            return String.join(File.pathSeparator, jars);
        } catch (IOException e) {
            throw new RuntimeException("Failed to build classpath", e);
        }
    }

    public List<String> buildCommand(LaunchOptions options) {
        List<String> command = new ArrayList<>();
        command.add("java");
        Collections.addAll(command, buildJvmArguments(options));
        command.add("-cp");
        Collections.addAll(command, buildClasspath());
        command.add("net.minecraft.client.main.Main");
        Collections.addAll(command, buildGameArguments(options));

        return command;
    }
}