package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class PlayerArgument extends CommandArgument<Player> {
    public PlayerArgument(CommandArgumentBuilder<Player> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Player> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public PlayerArgument build() {
                return new PlayerArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        return Bukkit.getPlayerExact(input) != null; // Check if player is online
    }

    @Override
    public Player convert(String input) {
        return Bukkit.getPlayerExact(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc(String.format("&cInvalid argument '%s'. Player is not online: '%s'", this.getName(), input));
    }

    @Override
    public List<String> getCustomSuggestions(CommandSender sender, String currentInput) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList()); // Provide online player names as suggestions
    }
}
