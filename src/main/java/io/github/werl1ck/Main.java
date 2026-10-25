package io.github.werl1ck;

import io.github.werl1ck.console.ConsoleUI;
import io.github.werl1ck.discord.DiscordRPC;


public class Main {
    public static void main(String[] args) {
        DiscordRPC discord = new DiscordRPC();

        try {
            discord.rpcConenct();
        } catch (Exception e) {
            System.out.println("Discord RPC unavailable. Continuing without it");
        }

        ConsoleUI consoleUI = new ConsoleUI(discord);
        try {
            consoleUI.ui();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
