package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class InfiniteStringArgument extends CommandArgument<String[]> {
    public InfiniteStringArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean isValid(String input) {
        return !input.isEmpty(); // Accepts any non-empty string
    }

    @Override
    public String[] convert(String input) {
        return input.split(" "); // Split by spaces to accept multiple strings
    }

    @Override
    public String getErrorMessage(String input) {
        return tc("Invalid input for infinite argument: " + input);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return List.of(); // No specific suggestions for infinite strings
    }
}
