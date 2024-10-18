package me.timjuice.roidCore.config;

import lombok.Getter;
import me.timjuice.roidCore.utils.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Config {

    private final ConfigLoader configLoader;

    // Configuration values
    private String helpMessageHeader;
    private String helpCategoryHeader;
    private String noPermissionMessage;
    private String onlyPlayersCommandMessage;
    private String invalidCommandMessage;

    public Config(JavaPlugin plugin) {
        // Initialize the config loader
        this.configLoader = new ConfigLoader(plugin);
        loadConfig(plugin);  // Load the configuration
    }

    /**
     * Load and validate all configuration options.
     */
    private void loadConfig(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        // Load each configuration value with validation
        helpMessageHeader = configLoader.getColoredString(config, "help-message-header");
        helpCategoryHeader = configLoader.getColoredString(config, "help-category-header");
        noPermissionMessage = configLoader.getColoredString(config, "no-permission-message");
        onlyPlayersCommandMessage = configLoader.getColoredString(config, "only-players-command");
        invalidCommandMessage = configLoader.getColoredString(config, "invalid-command-message");

        // Log successful loading
        ConsoleLogger.success(plugin, "Configuration successfully loaded.");
    }
}
