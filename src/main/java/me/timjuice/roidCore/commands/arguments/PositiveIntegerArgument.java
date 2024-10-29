package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class PositiveIntegerArgument extends IntegerArgument {
    public PositiveIntegerArgument(CommandArgumentBuilder<Integer> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Integer> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public PositiveIntegerArgument build() {
                return new PositiveIntegerArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        try {
            int value = Integer.parseInt(input);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getErrorMessage(String input) {
        return tc(String.format("&cInvalid positive integer input: '%s'. Must be a positive integer.", input));
    }
}
