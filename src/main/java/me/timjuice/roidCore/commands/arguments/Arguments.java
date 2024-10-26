package me.timjuice.roidCore.commands.arguments;

import me.timjuice.roidCore.utils.ConsoleLogger;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Optional;

/**
 * This class is used to store and retrieve command arguments.
 */
public class Arguments {

    /**
     * The map of the arguments.
     */
    private final HashMap<String, Object> arguments;

    /**
     * The map of the flags.
     */
    private final HashMap<String, Boolean> flags;

    /**
     * The plugin instance for logging purposes.
     */
    private final Plugin plugin;

    /**
     * Constructor of the class.
     * @param plugin The plugin instance used for logging.
     */
    public Arguments(Plugin plugin) {
        this.arguments = new HashMap<>();
        this.flags = new HashMap<>();
        this.plugin = plugin;
    }

    /**
     * Put an argument into the map.
     * @param key The key of the argument.
     * @param value The value of the argument.
     */
    public void put(String key, Object value) {
        arguments.put(key, value);
    }

    /**
     * Get an argument from the map.
     *
     * @param key The key of the argument.
     * @param <T> The type of the argument.
     * @return The argument value or null if it doesn't exist.
     */
    public <T> T get(String key) {
        try {
            Optional<T> value = this.getOptional(key);
            if (value.isEmpty()) {
                ConsoleLogger.error(plugin, "The argument '" + key + "' does not exist.");
                return null; // Return null if argument doesn't exist
            }
            return value.get();
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Error retrieving argument '" + key + "': " + e.getMessage());
        }
        return null; // Return null in case of an error
    }

    /**
     * Get an argument from the map as an Optional.
     *
     * @param key The key of the argument.
     * @param <T> The type of the argument.
     * @return An Optional containing the argument value if it exists, or an empty Optional.
     */
    public <T> Optional<T> getOptional(String key) {
        return Optional.ofNullable((T) arguments.get(key));
    }

    /**
     * Check if the map contains a specific argument.
     *
     * @param key The key of the argument.
     * @return True if the argument exists, false otherwise.
     */
    public boolean has(String key) {
        return arguments.containsKey(key);
    }

    /**
     * Get the amount of provided arguments
     *
     * @return amount of provided arguments
     */
    public int getAmount() {
        return arguments.size();
    }

    /**
     * Clear all arguments.
     */
    public void clear() {
        arguments.clear();
    }

    /**
     * Set a flag.
     *
     * @param key   The key of the flag.
     */
    public void setFlag(String key) {
        flags.put(key, true);
    }

    /**
     * Get a flag from the map.
     *
     * @param key The key of the flag.
     * @return The flag value or false if it doesn't exist.
     */
    public boolean getFlag(String key) {
        return flags.getOrDefault(key, false); // Return false if flag doesn't exist
    }

    /**
     * Check if the map contains a specific flag.
     *
     * @param key The key of the flag.
     * @return True if the flag exists, false otherwise.
     */
    public boolean hasFlag(String key) {
        return flags.containsKey(key);
    }

    /**
     * Clear all flags.
     */
    public void clearFlags() {
        flags.clear();
    }
}
