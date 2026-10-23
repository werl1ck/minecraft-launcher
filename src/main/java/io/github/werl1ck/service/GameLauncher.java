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
                "-Djava.library.path=" + versionInstall.getGameVersionDirectory(options.getGameVersion()).resolve(nativeFiles).toAbsolutePath(),
                buildRamArgument(options)
        };
    }

    public String[] buildGameArguments(LaunchOptions options, VersionInfo info) {
        Path versionDir = versionInstall.getGameVersionDirectory(options.getGameVersion());
        String gameDir = versionInstall.getGameVersionDirectory(options.getGameVersion()).toAbsolutePath().toString();
        String assetsDir = versionInstall.getGameVersionDirectory(options.getGameVersion()).resolve("assets").toAbsolutePath().toString();
        String assetIndex = (info.assetIndex() != null) ? info.assetIndex().id() : options.getGameVersion();


        List<String> rawArgs = new ArrayList<>();

        // old format
        if (info.minecraftArguments() != null) {
            for (String arg : info.minecraftArguments().split(" ")) {
                rawArgs.add(arg);
            }
        }

        // new format
        else if (info.arguments() != null && info.arguments().game() != null) {
            for (JsonElement element : info.arguments().game()) {
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
                        boolean isDemoUser = true;
                        JsonArray rules = obj.getAsJsonArray("rules");
                        for (JsonElement jsonElement : rules) {
                            JsonObject rule = jsonElement.getAsJsonObject();
                            String action = rule.get("action").getAsString();
                            boolean ruleMatches = true;
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
                            } else if (rule.has("os")) {
                                JsonObject os = rule.get("os").getAsJsonObject();
                                String osName = System.getProperty("os.name").toLowerCase();
                                String requiredOs = os.get("name").getAsString();
                                boolean osMatches = osName.contains(requiredOs);
                            }
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
                .replace("${quickPlaySingplayer}", "")
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
        Path jsonPath = versionInstall.getGameVersionDirectory(options.getGameVersion()).resolve(options.getGameVersion() + ".json");
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