package io.github.werl1ck;

public class Starter {
    public void main(String[] args) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("java", "-version");
        builder.inheritIO();
        Process process = builder.start();
        System.out.println("Exit code: " + process.waitFor());
    }
}
