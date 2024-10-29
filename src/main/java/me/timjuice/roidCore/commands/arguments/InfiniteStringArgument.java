package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class InfiniteStringArgument extends CommandArgument<String> {
    public InfiniteStringArgument(CommandArgumentBuilder<String> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<String> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public InfiniteStringArgument build() {
                return new InfiniteStringArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        return !input.trim().isEmpty(); // Accepts any non-empty string
    }

    @Override
    public String convert(String input) {
        return input; // Split by spaces to accept multiple strings
    }

    @Override
    public String getErrorMessage(String input) {
        return tc(String.format("&cInvalid input for infinite argument: '%s'", input));
    }
}
