package me.timjuice.roidCore.config;

import lombok.Getter;
import me.timjuice.roidCore.utils.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

@Getter
public class CoreMessageConfig {
    private String helpMessageHeader;
    private String helpCategoryHeader;
    private String noPermissionMessage;
    private String onlyPlayersCommandMessage;
    private String invalidCommandMessage;
    private String helpCommandGroupFormat;
    private String helpIndividualSubcommandFormat;

    public CoreMessageConfig(JavaPlugin plugin) {
        generateDefaultConfig(plugin);  // Generate default configuration if not exists
        loadDefaultConfig(plugin);  // Load the configuration
    }

    /**
     * Generates the default configuration file if it does not exist.
     */
    private void generateDefaultConfig(JavaPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!configFile.exists()) {
            try {
                // Create the directory if it doesn't exist
                plugin.getDataFolder().mkdirs();
                // Create the default config file
                configFile.createNewFile();

                // Write default values to the config file
                FileConfiguration config = plugin.getConfig();
                config.set("help-message-header", "&8&m--------------&8[ &a&l{PLUGIN_NAME} &8]&m--------------");
                config.set("help-category-header", "&8&m--------------&8[ &a&l{CATEGORY} &2&lHelp &8]&m--------------");
                config.set("no-permission-message", "&cYou don't have permission to use this!");
                config.set("only-players-command", "&cOnly players can use this command!");
                config.set("invalid-command-message", "&cThat command doesn't exist! Type /{PLUGIN_NAME} for help.");
                config.set("help-command-group-format", "&a/{BASE_CMD} help {CMD_GROUP} &f- &7Shows {CMD_GROUP} commands");
                config.set("help-individual-subcommand-format", "&a/{BASE_CMD} {SUB_CMD_NAME} &f- &7{SUB_CMD_DESCRIPTION}");

                // Save the config
                config.save(configFile);
                ConsoleLogger.success(plugin, "Default configuration created successfully.");
            } catch (IOException e) {
                ConsoleLogger.error(plugin, "Could not create default configuration: " + e.getMessage());
            }
        }
    }

    /**
     * Load and validate all configuration options.
     */
    private void loadDefaultConfig(JavaPlugin plugin) {
        ConfigLoader configLoader = new ConfigLoader(plugin, new File(plugin.getDataFolder(), "messages.yml"));
        // Load each configuration value with validation
        helpMessageHeader = configLoader.getColoredString("help-message-header");
        helpCategoryHeader = configLoader.getColoredString("help-category-header");
        noPermissionMessage = configLoader.getColoredString("no-permission-message");
        onlyPlayersCommandMessage = configLoader.getColoredString("only-players-command");
        invalidCommandMessage = configLoader.getColoredString("invalid-command-message");
        helpCommandGroupFormat = configLoader.getColoredString("help-command-group-format");
        helpIndividualSubcommandFormat = configLoader.getColoredString("help-individual-subcommand-format");

        // Log successful loading
        ConsoleLogger.success(plugin, "Configuration successfully loaded.");
    }
}
