package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class OfflinePlayerArgument extends CommandArgument<OfflinePlayer> {
    public OfflinePlayerArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean isValid(String input) {
        return Bukkit.getOfflinePlayer(input) != null; // Check if the player exists
    }

    @Override
    public OfflinePlayer convert(String input) {
        return Bukkit.getOfflinePlayer(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc("Offline player: " + input + " was not found!");
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> player.getName() != null && player.getName().toLowerCase().startsWith(currentInput.toLowerCase()))
                .map(OfflinePlayer::getName)
                .collect(Collectors.toList()); // Provide offline players as suggestions
    }
}
