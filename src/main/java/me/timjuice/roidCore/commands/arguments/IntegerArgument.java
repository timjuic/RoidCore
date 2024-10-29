package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

public class IntegerArgument extends CommandArgument<Integer> {
    public IntegerArgument(CommandArgumentBuilder<Integer> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Integer> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public IntegerArgument build() {
                return new IntegerArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
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
        return generateErrorMessage(input, "A positive number value (e.g., 10, 25).");
    }
}
