package me.timjuice.roidCore.commands.arguments;

import me.timjuice.roidCore.model.RoidPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CombinedPlayerArgument extends CommandArgument<RoidPlayer> {
    public CombinedPlayerArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean isValid(String input) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        return offlinePlayer != null && offlinePlayer.hasPlayedBefore();
    }

    @Override
    public RoidPlayer convert(String input) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        return new RoidPlayer(offlinePlayer.getUniqueId());
    }

    @Override
    public String getErrorMessage(String input) {
        return String.format("Invalid argument '%s'. Player '%s' was not found or has never played before!", this.getName(), input);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .toList());
    }
}