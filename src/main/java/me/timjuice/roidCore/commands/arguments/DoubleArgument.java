package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

public class DoubleArgument extends CommandArgument<Double> {
    public DoubleArgument(CommandArgumentBuilder<Double> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Double> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public DoubleArgument build() {
                return new DoubleArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public Double convert(String input) {
        return Double.parseDouble(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return generateErrorMessage(input, "A valid double value (e.g., 10.5)");
    }
}
