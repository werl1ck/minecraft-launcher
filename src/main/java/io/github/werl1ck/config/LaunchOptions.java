package io.github.werl1ck.config;

public class LaunchOptions {
    private final String gameVersion;
    private final String nickname;
    private final int jvmRam;

    public LaunchOptions(String gameVersion, String nickname, int jvmRam) {

        String regex = "^[a-zA-Z0-9_]+$";

        if (nickname == null || nickname.length() > 16 || nickname.isEmpty()) {
            throw new IllegalArgumentException("Invalid nickname format");
        }

        if (!nickname.matches(regex)) {
            throw new IllegalArgumentException("The nickname must contain only English letters");
        }

        if (jvmRam <= 0) {
            throw new IllegalArgumentException("RAM cannot be zero or empty, a standard of 1024 megabytes has been set");
        }

        if (gameVersion == null || gameVersion.isEmpty()) {
            throw new IllegalArgumentException("You have entered an incorrect game version; please double-check your input");
        }

        this.gameVersion = gameVersion;
        this.nickname = nickname;
        this.jvmRam = jvmRam;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public String getNickname() {
        return nickname;
    }

    public int getJvmRam() {
        return jvmRam;
    }
}
