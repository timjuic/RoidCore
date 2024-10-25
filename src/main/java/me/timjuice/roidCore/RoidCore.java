package me.timjuice.roidCore;

import lombok.Getter;
import me.timjuice.roidCore.commands.CommandManager;
import me.timjuice.roidCore.commands.TestCommand;
import me.timjuice.roidCore.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class RoidCore extends JavaPlugin {
    @Getter
    private static RoidCore instance;
    @Getter
    private Config conf;

    private CommandManager testCommandManager;

    @Override
    public void onEnable() {
        instance = this;
        conf = new Config(this);

        testCommandManager = new CommandManager(this, "roidcore");
        testCommandManager.addCommand(new TestCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
