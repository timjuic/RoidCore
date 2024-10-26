package me.timjuice.roidCore.config;

import lombok.Getter;
import me.timjuice.roidCore.RoidCore;
import me.timjuice.roidCore.utils.ConsoleLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@Getter
public class CoreMessageConfig {
    protected final RoidCore roidPlugin;
    protected File configFile;
    protected FileConfiguration config;

    private String messagePrefix;
    private String helpMessageHeader;
    private String helpCategoryHeader;
    private String noPermissionMessage;
    private String onlyPlayersCommandMessage;
    private String invalidCommandMessage;
    private String helpCommandGroupFormat;
    private String helpIndividualSubcommandFormat;

    public CoreMessageConfig(RoidCore roidPlugin) {
        this.roidPlugin = roidPlugin;
        generateDefaultConfig();  // Generate default configuration if not exists
        loadDefaultConfig();  // Load the configuration
    }

    /**
     * Generates the default configuration file if it does not exist.
     */
    private void generateDefaultConfig() {
        configFile = new File(roidPlugin.getDataFolder(), "messages.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Add default values
        config.addDefault("message-prefix", "&f&lRoidCore &8» ");
        config.addDefault("help-message-header", "&8&m--------------&8[ &a&l{PLUGIN_NAME} &8]&m--------------");
        config.addDefault("help-category-header", "&8&m--------------&8[ &a&l{CATEGORY} &2&lHelp &8]&m--------------");
        config.addDefault("no-permission-message", "&cYou don't have permission to use this!");
        config.addDefault("only-players-command", "&cOnly players can use this command!");
        config.addDefault("invalid-command-message", "&cThat command doesn't exist! Type /{PLUGIN_NAME} for help.");
        config.addDefault("help-command-group-format", "&a/{BASE_CMD} help {CMD_GROUP} &f- &7Shows {CMD_GROUP} commands");
        config.addDefault("help-individual-subcommand-format", "&a/{BASE_CMD} {SUB_CMD_NAME} &f- &7{SUB_CMD_DESCRIPTION}");

        // If the file doesn't exist, save defaults and create the file
        if (!configFile.exists()) {
            roidPlugin.getDataFolder().mkdirs();
            try {
                config.save(configFile);
                ConsoleLogger.success(roidPlugin, "Default configuration created successfully.");
            } catch (IOException e) {
                ConsoleLogger.error(roidPlugin, "Could not create default configuration: " + e.getMessage());
            }
        } else {
            // If the file exists, just save any new defaults added
            config.options().copyDefaults(true);
            try {
                config.save(configFile);
                ConsoleLogger.success(roidPlugin, "Configuration updated with default values.");
            } catch (IOException e) {
                ConsoleLogger.error(roidPlugin, "Could not update configuration: " + e.getMessage());
            }
        }
    }

    /**
     * Load and validate all configuration options.
     */
    private void loadDefaultConfig() {
        ConfigLoader configLoader = new ConfigLoader(roidPlugin, new File(roidPlugin.getDataFolder(), "messages.yml"));
        // Load each configuration value with validation
        messagePrefix = configLoader.getColoredString("message-prefix");
        helpMessageHeader = configLoader.getColoredString("help-message-header");
        helpCategoryHeader = configLoader.getColoredString("help-category-header");
        noPermissionMessage = configLoader.getColoredString("no-permission-message");
        onlyPlayersCommandMessage = configLoader.getColoredString("only-players-command");
        invalidCommandMessage = configLoader.getColoredString("invalid-command-message");
        helpCommandGroupFormat = configLoader.getColoredString("help-command-group-format");
        helpIndividualSubcommandFormat = configLoader.getColoredString("help-individual-subcommand-format");

        // Log successful loading
        ConsoleLogger.success(roidPlugin, "Configuration successfully loaded.");
    }
}
