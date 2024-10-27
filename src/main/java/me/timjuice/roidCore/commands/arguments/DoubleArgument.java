package me.timjuice.roidCore.commands.arguments;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class DoubleArgument extends CommandArgument<Double> {
    public DoubleArgument(String name, boolean required) {
        super(name, required);
    }

    public DoubleArgument(String name) {
        super(name);
    }

    @Override
    public boolean isValid(String input) {
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

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return List.of(); // No specific suggestions for longs
    }
}
