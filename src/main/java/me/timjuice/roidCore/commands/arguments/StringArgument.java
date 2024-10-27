package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class StringArgument extends CommandArgument<String> {
    private final List<String> options;

    public StringArgument(String name, boolean required, String... options) {
        super(name, required);
        this.options = options.length > 0 ? Arrays.asList(options) : Collections.emptyList();
    }

    public StringArgument(String name, String... options) {
        super(name);
        this.options = options.length > 0 ? Arrays.asList(options) : Collections.emptyList();
    }

    @Override
    public boolean isValid(String input) {
        return options.isEmpty() || options.contains(input); // Validate against predefined options, or allow any string if no options
    }

    @Override
    public String convert(String input) {
        return input; // No conversion needed
    }

    @Override
    public String getErrorMessage(String input) {
        if (options.isEmpty()) {
            return tc(String.format("&cInvalid argument: '%s'", input));
        }
        return tc(String.format("&cInvalid argument: '%s'. Expected: %s", input, String.join(", ", options)));
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        if (options.isEmpty()) {
            return Collections.emptyList(); // No suggestions if no options are defined
        }
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase())) // Suggest options based on input
                .collect(Collectors.toList());
    }
}
