package me.timjuice.roidCore.utils;

import org.bukkit.ChatColor;

public class FormatUtil {
    public static String tc(String textToTranslateColors) {
        return ChatColor.translateAlternateColorCodes('&', textToTranslateColors);
    }
}
