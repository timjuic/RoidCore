package me.timjuice.roidCore;

import lombok.Getter;
import me.timjuice.roidCore.commands.CommandManager;
import me.timjuice.roidCore.commands.TestCommand;
import me.timjuice.roidCore.config.CoreMessageConfig;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class RoidCore extends JavaPlugin {
    @Getter
    private static RoidCore instance;
    @Getter
    private CoreMessageConfig messageConfig;

    protected CommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;
        messageConfig = new CoreMessageConfig(this);

        commandManager = new CommandManager(this, "roidcore");
        commandManager.addCommand(new TestCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
