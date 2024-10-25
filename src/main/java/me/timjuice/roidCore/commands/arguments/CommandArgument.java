package me.timjuice.roidCore.commands.arguments;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.List;

@Getter
public abstract class CommandArgument<T> {
    private final String name;
    private final boolean required;

    public CommandArgument(String name, boolean required) {
        this.name = name;
        this.required = required; // Set the required attribute
    }

    // Abstract method to validate the argument
    public abstract boolean isValid(String input);

    // Abstract method to convert the argument
    public abstract T convert(String input);

    // Optional: Abstract method for error message (if needed)
    public abstract String getErrorMessage(String input);

    // Optional: Abstract method for tab completion suggestions (if needed)
    public abstract List<String> getSuggestions(CommandSender sender, String currentInput);

    public String getUsage() {
        return required ? "<" + name + ">" : "[" + name + "]";
    }
}
