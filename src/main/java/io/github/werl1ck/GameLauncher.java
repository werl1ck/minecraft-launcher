package io.github.werl1ck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLauncher {
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
        return new String[]{buildRamArgument(options)};
    }

    public String[] buildGameArguments(LaunchOptions options) {
        return new String[]{
                "--username", options.getNickname(),
                "--version", options.getGameVersion()
        };
    }

    public List<String> buildCommand(LaunchOptions options) {
        List<String> command = new ArrayList<>();
        Collections.addAll(command, buildJvmArguments(options));
        Collections.addAll(command, buildGameArguments(options));

        return command;
    }
}