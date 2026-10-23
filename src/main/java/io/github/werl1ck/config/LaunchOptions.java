package io.github.werl1ck.config;

public class LaunchOptions {
    private final String gameVersion;
    private final String nickname;
    private final int jvmRam;

    public LaunchOptions(String gameVersion, String nickname, int  jvmRam) {
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
