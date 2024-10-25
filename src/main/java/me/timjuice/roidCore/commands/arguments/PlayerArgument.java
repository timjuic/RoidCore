package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class PlayerArgument extends CommandArgument<Player> {
    public PlayerArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean isValid(String input) {
        return Bukkit.getPlayer(input) != null; // Check if player is online
    }

    @Override
    public Player convert(String input) {
        return Bukkit.getPlayer(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc("Player not found: " + input);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList()); // Provide online player names as suggestions
    }
}
