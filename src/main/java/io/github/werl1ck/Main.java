package io.github.werl1ck;

public class Main {
    public static void main(String[] args) {
        ConsoleUI consoleUI = new ConsoleUI();
        try {
            consoleUI.ui();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
