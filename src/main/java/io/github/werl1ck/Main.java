package io.github.werl1ck;

import io.github.werl1ck.console.ConsoleUI;


public class Main {
    static void main(String[] args) {
        ConsoleUI consoleUI = new ConsoleUI();
        try {
            consoleUI.ui();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
