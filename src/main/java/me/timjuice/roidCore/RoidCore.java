package me.timjuice.roidCore;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class RoidCore extends JavaPlugin {
    @Getter
    private static RoidCore instance;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
