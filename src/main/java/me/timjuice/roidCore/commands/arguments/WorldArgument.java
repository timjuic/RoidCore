package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class WorldArgument extends CommandArgument<World> {
    public WorldArgument(CommandArgumentBuilder<World> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<World> builder(String name) {
        return new CommandArgumentBuilder<>(name);
    }

    @Override
    public boolean isTypeValid(String input) {
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
