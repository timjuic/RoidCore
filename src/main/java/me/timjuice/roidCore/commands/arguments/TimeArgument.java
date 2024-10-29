package me.timjuice.roidCore.commands.arguments;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeArgument extends CommandArgument<Duration> {
    public TimeArgument(CommandArgumentBuilder<Duration> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Duration> builder(String name) {
        return new CommandArgumentBuilder<>(name);
    }

    @Override
    public boolean isTypeValid(String input) {
        try {
            convert(input);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public Duration convert(String input) {
        return parseDuration(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return generateErrorMessage(input, "valid time duration (e.g., 10d, 5h, 10d5h5m53s, 1y, 10days, 1year2months3days)");
    }

    private static Duration parseDuration(String input) {
        // Regex to match various time formats
        Pattern pattern = Pattern.compile("(\\d+)\\s*(s|m|h|d|mo|y|days?|months?|years?)");

        Duration duration = Duration.ZERO;
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            switch (unit) {
                case "s":
                case "seconds":
                    duration = duration.plusSeconds(value);
                    break;
                case "m":
                case "minutes":
                    duration = duration.plusMinutes(value);
                    break;
                case "h":
                case "hours":
                    duration = duration.plusHours(value);
                    break;
                case "d":
                case "days":
                    duration = duration.plusDays(value);
                    break;
                case "mo":
                case "months":
                    duration = duration.plusDays(value * 30L); // Assuming 30 days per month
                    break;
                case "y":
                case "years":
                    duration = duration.plusDays(value * 365L); // Assuming 365 days per year
                    break;
                default:
                    throw new DateTimeParseException("Invalid time unit: " + unit, input, 0);
            }
        }

        return duration;
    }
}
