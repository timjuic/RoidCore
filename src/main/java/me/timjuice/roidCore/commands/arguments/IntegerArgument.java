package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class IntegerArgument extends CommandArgument<Integer> {
    public IntegerArgument(String name, boolean required) {
        super(name, required);
    }

    @Override
    public boolean isValid(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Integer convert(String input) {
        return Integer.parseInt(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc("Invalid integer input: " + input);
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return List.of();
    }
}
