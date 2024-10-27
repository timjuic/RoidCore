package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

public class ShortArgument extends CommandArgument<Short> {
    public ShortArgument(String name, boolean required) {
        super(name, required);
    }

    public ShortArgument(String name) {
        super(name);
    }

    @Override
    public boolean isValid(String input) {
        try {
            Short.parseShort(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Short convert(String input) {
        return Short.parseShort(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return generateErrorMessage(input, "A short number value (e.g., 5, 20).");
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return List.of();
    }
}
