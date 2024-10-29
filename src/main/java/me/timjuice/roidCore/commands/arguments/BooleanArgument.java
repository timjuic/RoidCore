package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanArgument extends CommandArgument<Boolean> {

    public BooleanArgument(CommandArgumentBuilder<Boolean> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Boolean> builder(String name) {
        return new CommandArgumentBuilder<Boolean>(name) {
            @Override
            public BooleanArgument build() {
                return new BooleanArgument(this);
            }
        };
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
    public List<String> getCustomSuggestions(CommandSender sender, String currentInput) {
        // Suggest "true", "false", "yes", and "no" that match the current input
        return Stream.of("true", "false", "yes", "no")
                .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList());
    }
}
