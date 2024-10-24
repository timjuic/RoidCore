package me.timjuice.roidCore.config;

import me.timjuice.roidCore.utils.ConsoleLogger;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigLoader {
    private final JavaPlugin plugin;
    private final File configFile;
    private final FileConfiguration config;

    public ConfigLoader(JavaPlugin plugin, File configFile) {
        this.plugin = plugin;
        this.configFile = configFile;
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Safely loads an integer from the config, with validation and default value support.
     */
    public int getInt(String path, int defaultValue) {
        if (config.contains(path)) {
            try {
                return config.getInt(path);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin, "Invalid int format at path: " + path + ", using default value: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public int getInt(String path) {
        return getInt(path, 0);
    }

    /**
     * Safely loads a double from the config, with validation and default value support.
     */
    public double getDouble(String path, double defaultValue) {
        if (config.contains(path)) {
            try {
                return config.getDouble(path);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin, "Invalid double format at path: " + path + ", using default value: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    /**
     * Safely loads a long from the config, with validation and default value support.
     */
    public long getLong(String path, long defaultValue) {
        if (config.contains(path)) {
            try {
                return config.getLong(path);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin, "Invalid long format at path: " + path + ", using default value: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public long getLong(String path) {
        return getLong(path, 0L);
    }

    /**
     * Safely loads a boolean from the config, with validation and default value support.
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        if (config.contains(path)) {
            try {
                return config.getBoolean(path);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin, "Invalid boolean format at path: " + path + ", using default value: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    /**
     * Safely loads a String from the config, with validation and default value support.
     */
    public String getString(String path, String defaultValue) {
        if (config.contains(path)) {
            try {
                return config.getString(path);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin,"Invalid string format at path: " + path + ", using default value: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public String getString(String path) {
        return getString(path, null);
    }

    /**
     * Safely loads a list of strings from the config, with validation and default value support.
     */
    public List<String> getStringList(String path, List<String> defaultValue) {
        if (config.contains(path)) {
            try {
                return config.getStringList(path);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin, "Invalid list format at path: " + path + ", using default value.");
            }
        }
        return defaultValue;
    }

    public List<String> getStringList(String path) {
        return getStringList(path, new ArrayList<>());
    }

    /**
     * Safely loads a String from the config, with validation and default value support.
     */
    public String getColoredString(String path) {
        if (config.contains(path)) {
            try {
                return ChatColor.translateAlternateColorCodes('&', config.getString(path));
            } catch (Exception e) {
                ConsoleLogger.warning(plugin,"Invalid string format at path: " + path);
            }
        }
        return "";
    }

    /**
     * Safely loads a ConfigurationSection from the config, with error handling.
     *
     * @param path The path to the ConfigurationSection.
     * @return The ConfigurationSection, or null if not found.
     */
    public ConfigurationSection getConfigurationSection(String path) {
        if (config.contains(path)) {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section != null) {
                return section;
            } else {
                ConsoleLogger.warning(plugin, "Configuration section at path '" + path + "' is null.");
            }
        } else {
            ConsoleLogger.warning(plugin, "Configuration section at path '" + path + "' does not exist.");
        }
        return null; // Return null if the section is not found or is invalid
    }

    /**
     * Safely loads a List<String> from the with color code translation and validation.
     *
     * @param path   The path in the config to retrieve the list.
     * @return A list of color-translated strings, or an empty list if the path is invalid or not found.
     */
    public List<String> getColoredStringList(String path) {
        if (config.contains(path)) {
            List<String> rawList = config.getStringList(path);

            // Translate color codes for each string in the list
            return rawList.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList());
        }

        // If path doesn't exist, log a warning and return an empty list
        ConsoleLogger.warning(plugin, "Missing or invalid list at path: " + path + ". Returning an empty list.");
        return Collections.emptyList();  // Safely return an empty list
    }

    /**
     * Safely loads a Material from the config, with validation and default value support.
     */
    public Material getMaterial(String path, Material defaultValue) {
        if (config.contains(path)) {
            String materialName = config.getString(path);
            try {
                return Material.matchMaterial(materialName);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin, "Invalid material format at path: " + path + ", using default material: " + defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Safely loads a Location from the config with optional yaw and pitch support.
     */
    public Location getLocation(String path, World world, Location defaultLocation) {
        if (config.contains(path)) {
            try {
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                float yaw = (float) config.getDouble(path + ".yaw", 0.0);
                float pitch = (float) config.getDouble(path + ".pitch", 0.0);
                return new Location(world, x, y, z, yaw, pitch);
            } catch (Exception e) {
                ConsoleLogger.warning(plugin,"Invalid location format at path: " + path + ", using default location.");
            }
        }
        return defaultLocation;
    }

    /**
     * Safely loads an ItemStack from the config, with validation and default value support.
     */
    public ItemStack getItemStack(String path, ItemStack defaultItem) {
        if (config.contains(path)) {
            try {
                String materialName = config.getString(path + ".material", "STONE");
                Material material = Material.matchMaterial(materialName);
                if (material == null) material = Material.STONE;  // Fallback if material is invalid
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    String displayName = config.getString(path + ".name", null);
                    if (displayName != null) meta.setDisplayName(displayName);

                    List<String> lore = config.getStringList(path + ".lore");
                    if (lore != null) meta.setLore(lore);

                    item.setItemMeta(meta);
                }

                return item;
            } catch (Exception e) {
                ConsoleLogger.warning(plugin,"Invalid ItemStack format at path: " + path + ", using default item.");
            }
        }
        return defaultItem;
    }

    public void saveConfigAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Save the entire config object to the config file
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception as needed
            }
        });
    }
}
