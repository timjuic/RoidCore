package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class LongArgument extends CommandArgument<Long> {
    public LongArgument(CommandArgumentBuilder<Long> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Long> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public LongArgument build() {
                return new LongArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Long convert(String input) {
        return Long.parseLong(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return tc(String.format("&cInvalid long input: '%s'", input));
    }
}
