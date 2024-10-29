package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

public class ShortArgument extends CommandArgument<Short> {
    public ShortArgument(CommandArgumentBuilder<Short> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Short> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public ShortArgument build() {
                return new ShortArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
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
}
