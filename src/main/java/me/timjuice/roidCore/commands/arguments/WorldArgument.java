package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class WorldArgument extends CommandArgument<World> {

    public WorldArgument(String name, boolean required) {
        super(name, required);
    }

    public WorldArgument(String name) {
        super(name);
    }

    @Override
    public boolean isValid(String input) {
        return Bukkit.getWorld(input) != null;
    }

    @Override
    public World convert(String input) {
        return Bukkit.getWorld(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return String.format("World '%s' not found. Please specify a valid world name.", input);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        // List all worlds that match the current input
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList());
    }
}
