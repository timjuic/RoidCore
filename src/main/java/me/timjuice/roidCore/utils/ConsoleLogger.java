package me.timjuice.roidCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ConsoleLogger {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN_BRIGHT = "\u001B[32;1m";

    // Plugin instance is passed to use the correct plugin name
    public static void info(JavaPlugin plugin, String message) {
        Bukkit.getLogger().info(String.format("[%s] %s", plugin.getName(), message));
    }

    public static void warning(JavaPlugin plugin, String message) {
        Bukkit.getLogger().warning(YELLOW + String.format("[%s] %s", plugin.getName(), message) + RESET);
    }

    public static void error(JavaPlugin plugin, String message) {
        Bukkit.getLogger().severe(RED + String.format("[%s] %s", plugin.getName(), message) + RESET);
    }

    public static void success(JavaPlugin plugin, String message) {
        Bukkit.getLogger().info(GREEN_BRIGHT + String.format("[%s] %s", plugin.getName(), message) + RESET);
    }
}
