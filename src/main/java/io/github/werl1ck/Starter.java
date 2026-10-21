package io.github.werl1ck;

public class Starter {
    public void main(String[] args) throws Exception {
        final GameLauncher launcher;
        ProcessBuilder builder = new ProcessBuilder(launcher.buildCommand(new LaunchOptions(
                "1.12.2",
                "WerLick",
                1024
        )));
        builder.inheritIO();
        Process process = builder.start();
        System.out.println("Exit code: " + process.waitFor());
    }
}
