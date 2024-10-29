package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class OfflinePlayerArgument extends CommandArgument<OfflinePlayer> {
    public OfflinePlayerArgument(CommandArgumentBuilder<OfflinePlayer> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<OfflinePlayer> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public OfflinePlayerArgument build() {
                return new OfflinePlayerArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) return false;
        return true;
    }

    @Override
    public OfflinePlayer convert(String input) {
        return Bukkit.getOfflinePlayer(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc(String.format("&cInvalid argument '%s'. Offline player: '%s' was not found!", this.getName(), input));
    }

    @Override
    public List<String> getCustomSuggestions(CommandSender sender, String currentInput) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList()); // Provide online player names as suggestions
    }
}
