package me.timjuice.roidCore;

import lombok.Getter;
import me.timjuice.roidCore.config.Config;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class RoidCore extends JavaPlugin {
    @Getter
    private static RoidCore instance;
    private Config config;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
