package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BooleanArgument extends CommandArgument<Boolean> {

    public BooleanArgument(CommandArgumentBuilder<Boolean> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Boolean> builder(String name) {
        return new CommandArgumentBuilder<>(name);
    }

    @Override
    public boolean isTypeValid(String input) {
        // Check if input matches true/false or yes/no (case insensitive)
        return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false") ||
                input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("no");
    }

    @Override
    public Boolean convert(String input) {
        // Return true for "true" or "yes", and false for "false" or "no"
        return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes");
    }

    @Override
    public String getErrorMessage(String input) {
        return generateErrorMessage(input, "true/false or yes/no");
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        // Suggest "true", "false", "yes", and "no" that match the current input
        return Arrays.asList("true", "false", "yes", "no").stream()
                .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList());
    }
}
