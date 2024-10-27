package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeArgument extends CommandArgument<Long> {

    // Regex to match time inputs with multiple units (e.g., "10d12h5s" or "5y40d")
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+\\s*[smhd]|\\d+\\s*seconds?|\\d+\\s*minutes?|\\d+\\s*hours?|\\d+\\s*days?|\\d+\\s*months?|\\d+\\s*years?|\\d+\\s*y|\\d+\\s*mo)+");

    private final List<String> suggestions;

    public TimeArgument(String name, boolean required, List<String> suggestions) {
        super(name, required);
        this.suggestions = suggestions != null ? suggestions : Collections.emptyList();
    }

    public TimeArgument(String name, List<String> suggestions) {
        super(name);
        this.suggestions = suggestions != null ? suggestions : Collections.emptyList();
    }

    public TimeArgument(String name) {
        super(name);
        this.suggestions = Collections.emptyList();
    }

    public TimeArgument(String name, boolean required) {
        this(name, required, Collections.emptyList());
    }

    @Override
    public boolean isValid(String input) {
        // Check if the input matches the pattern
        return TIME_PATTERN.matcher(input).matches();
    }

    @Override
    public Long convert(String input) {
        long totalSeconds = 0;

        // Split the input into individual components (e.g., "10d", "12h", "5s")
        String[] parts = input.split("(?<=[smhd]|seconds|minutes|hours|days|months|years])\\s*");

        for (String part : parts) {
            part = part.trim(); // Clean up any extra spaces
            Matcher matcher = Pattern.compile("(\\d+)\\s*([smhd]|seconds?|minutes?|hours?|days?|months?|years?|y|mo)").matcher(part);

            if (matcher.matches()) {
                long value = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2).toLowerCase();

                switch (unit) {
                    case "s": case "seconds": totalSeconds += value; break; // seconds
                    case "m": case "minutes": totalSeconds += value * 60; break; // minutes to seconds
                    case "h": case "hours": totalSeconds += value * 3600; break; // hours to seconds
                    case "d": case "days": totalSeconds += value * 86400; break; // days to seconds
                    case "mo": case "months": totalSeconds += value * 2592000; break; // months to seconds (30 days)
                    case "y": case "years": totalSeconds += value * 31536000; break; // years to seconds (365 days)
                    default: throw new IllegalArgumentException("Invalid time unit.");
                }
            } else {
                throw new IllegalArgumentException("Invalid time format: " + part);
            }
        }

        return totalSeconds; // Return the total time in seconds
    }

    @Override
    public String getErrorMessage(String input) {
        return generateErrorMessage(input, "10s, 5m, 1h, 2d, 1mo, or mixed formats like 10d12h5s.");
    }

    @Override
    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        return suggestions; // Return the custom suggestions
    }
}
