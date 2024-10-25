package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class LongArgument extends CommandArgument<Long> {
    public LongArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean isValid(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Long convert(String input) {
        return Long.parseLong(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc(String.format("&cInvalid long input: '%s'", input));
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return List.of(); // No specific suggestions for longs
    }
}
