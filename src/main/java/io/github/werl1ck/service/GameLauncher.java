package io.github.werl1ck.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.werl1ck.config.LaunchOptions;
import io.github.werl1ck.model.VersionInfo;
import io.github.werl1ck.parser.JsonVersionReader;

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
                "-Djava.library.path=" + versionInstall.getGameVersionDirectory(options.getGameVersion())
                        .resolve(nativeFiles).toAbsolutePath(),
//                "--enable-native-access=ALL-UNNAMED",
//                "--enable-final-field-mutation=ALL-UNNAMED",
                buildRamArgument(options)
        };
    }

    public String[] buildGameArguments(LaunchOptions options, VersionInfo info) throws IOException {
        String gameDir = versionInstall.getGameVersionDirectory(options.getGameVersion()).toAbsolutePath().toString();
        String assetsDir = versionInstall.getGameVersionDirectory(options.getGameVersion()).resolve("assets").toAbsolutePath().toString();
        Path versionDir = versionInstall.getGameVersionDirectory(options.getGameVersion());
        Path vanillaJsonPath = versionDir.resolve(options.getGameVersion() + ".json");

        if (!Files.isRegularFile(vanillaJsonPath)) {
            throw new IOException("Base Minecraft JSON not found: " + vanillaJsonPath);
        }

        VersionInfo vanillaInfo = JsonVersionReader.read(vanillaJsonPath);
        String assetIndex;

        if (info.assetIndex() != null && info.assetIndex().id() != null) {
            assetIndex = info.assetIndex().id();
        } else if (vanillaInfo.assetIndex() != null
        && vanillaInfo.assetIndex().id() != null) {
            assetIndex = vanillaInfo.assetIndex().id();
        } else {
            throw new IOException("Asset index not found for version: " + options.getGameVersion());
        }

        VersionInfo argumentsInfo = info;
        if (info.minecraftArguments() == null
        && (info.arguments() == null
        || info.arguments().game() == null
        || info.arguments().game().isEmpty())) {
            argumentsInfo =  vanillaInfo;
        }

        List<String> rawArgs = new ArrayList<>();

        // old format
        if (argumentsInfo.minecraftArguments() != null) {
            for (String arg : argumentsInfo.minecraftArguments().split(" ")) {
                rawArgs.add(arg);
            }
        }

        // new format
        else if (argumentsInfo.arguments() != null && argumentsInfo.arguments().game() != null) {
            for (JsonElement element : argumentsInfo.arguments().game()) {
                if (element.isJsonPrimitive()) {
                    rawArgs.add(element.getAsString());
                } else if (element.isJsonArray()) {
                    for (JsonElement arrayElement : element.getAsJsonArray()) {
                        rawArgs.add(arrayElement.getAsString());
                    }
                } else if (element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    boolean allowed = false;

                    if (obj.has("rules")) {
                        boolean isDemoUser = false;
                        JsonArray rules = obj.getAsJsonArray("rules");
                        for (JsonElement jsonElement : rules) {
                            JsonObject rule = jsonElement.getAsJsonObject();
                            String action = rule.get("action").getAsString();
                            if (rule.has("features")) {
                                JsonObject features = rule.get("features").getAsJsonObject();
                                if (features.has("is_demo_user")) {
                                    boolean ruleIsDemo = features.get("is_demo_user").getAsBoolean();

                                    if (ruleIsDemo == isDemoUser && action.equals("allow")) {
                                        allowed = true;
                                    } else if (action.equals("disallow")) {
                                        allowed = false;
                                    }
                                }
                            }
                            /*

                            death code, todo: add multi-platform support

                            else if (rule.has("os")) {
                                JsonObject os = rule.get("os").getAsJsonObject();
                                String osName = System.getProperty("os.name").toLowerCase();
                                String requiredOs = os.get("name").getAsString();
                                boolean osMatches = osName.contains(requiredOs);
                            }

                            */
                        }
                    }

                    if (obj.has("value") && (!obj.has("rules") || allowed)) {
                        JsonElement value = obj.get("value");
                        if (value.isJsonPrimitive()) {
                            rawArgs.add(obj.get("value").getAsString());
                        } else if (value.isJsonArray()) {
                            for (JsonElement arrayElement : value.getAsJsonArray()) {
                                rawArgs.add(arrayElement.getAsString());
                            }
                        }
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException(
                    "No minecraftArguments or arguments found in the version JSON"
            );
        }

        List<String> finalArgs = new ArrayList<>();
        for (String arg : rawArgs) {
            finalArgs.add(applyReplacements(arg, options, gameDir, assetsDir, assetIndex, info.type()));
        }

        return finalArgs.toArray(new String[0]);
    }

    private String applyReplacements(String arg,
                                     LaunchOptions options,
                                     String gameDir,
                                     String assetsDir,
                                     String assetsIndex,
                                     String versionType)
    {
        return arg.replace("${auth_player_name}", options.getNickname())
                .replace("${version_name}", options.getGameVersion())
                .replace("${game_directory}", gameDir)
                .replace("${assets_root}", assetsDir)
                .replace("${assets_index_name}", assetsIndex)
                .replace("${auth_uuid}", "00000000-0000-0000-0000-000000000000")
                .replace("${auth_access_token}", "0")
                .replace("${user_type}", "legacy")
                .replace("${version_type}", versionType)
                .replace("${clientid}", "")
                .replace("${auth_xuid}", "0")
                .replace("${resolution_width}", "854")
                .replace("${resolution_height}", "480")
                .replace("${quickPlayPath}", "")
                .replace("${quickPlaySingleplayer}", "")
                .replace("${quickPlayMultiplayer}", "")
                .replace("${quickPlayRealms}", "");
    }

    public String buildClasspath(LaunchOptions options) {
        var pathToClientJar = versionInstall.getGameVersionDirectory(options.getGameVersion()).resolve("client.jar").toString();
        var librariesDir = versionInstall.getGameVersionDirectory(options.getGameVersion()).resolve("libraries");

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

    public List<String> buildCommand(LaunchOptions options) throws IOException {

        Path versionDir = versionInstall.getGameVersionDirectory(options.getGameVersion());
        Path jsonPath;
        String gameType = options.getGameType();

        if ("fabric".equalsIgnoreCase(gameType)) {
            jsonPath = Files.list(versionDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains("fabric-loader")
                            && path.getFileName().toString().contains(options.getGameVersion()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Fabric JSON file not found for version %s"
                            .formatted(options.getGameVersion())));
        } else {
            jsonPath = versionDir.resolve(options.getGameVersion() + ".json");
            if (!Files.exists(jsonPath)) {
                throw new RuntimeException("Vanilla JSON file not found: %s"
                        .formatted(jsonPath));
            }

            System.out.println("Using JSON: %s"
                    .formatted(jsonPath.getFileName()));
        }

        VersionInfo info = JsonVersionReader.read(jsonPath);

        List<String> command = new ArrayList<>();
        command.add("java");
        Collections.addAll(command, buildJvmArguments(options));
        command.add("-cp");
        command.add(buildClasspath(options));
        command.add(info.mainClass());
        Collections.addAll(command, buildGameArguments(options, info));

        return command;
    }
}