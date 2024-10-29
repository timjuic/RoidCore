package me.timjuice.roidCore.commands.arguments;

import me.timjuice.roidCore.model.RoidPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class CombinedPlayerArgument extends CommandArgument<RoidPlayer> {
    public CombinedPlayerArgument(CommandArgumentBuilder<RoidPlayer> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<RoidPlayer> builder(String name) {
        return new CommandArgumentBuilder<>(name);
    }

    @Override
    public boolean isTypeValid(String input) {
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
        return tc( String.format("Invalid argument '%s'. Player '%s' was not found or has never played before!", this.getName(), input));
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return new ArrayList<>(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .toList());
    }
}
