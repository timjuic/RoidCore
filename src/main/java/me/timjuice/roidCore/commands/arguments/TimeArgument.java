package me.timjuice.roidCore.commands.arguments;

import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeArgument extends CommandArgument<Duration> {
    private static final Pattern TIME_PATTERN = Pattern.compile(
        "^(?:\\d+\\s*(?:y(?:ear)?s?|mo(?:nth)?s?|d(?:ay)?s?|h(?:our)?s?|m(?:inute)?s?|s(?:econd)?s?)\\s*)+$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern UNIT_PATTERN = Pattern.compile(
        "(\\d+)\\s*(y(?:ear)?s?|mo(?:nth)?s?|d(?:ay)?s?|h(?:our)?s?|m(?:inute)?s?|s(?:econd)?s?)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Map<String, TemporalUnit> UNIT_MAPPING = new HashMap<>();
    static {
        // Years
        UNIT_MAPPING.put("y", ChronoUnit.YEARS);
        UNIT_MAPPING.put("year", ChronoUnit.YEARS);
        UNIT_MAPPING.put("years", ChronoUnit.YEARS);

        // Months
        UNIT_MAPPING.put("mo", ChronoUnit.MONTHS);
        UNIT_MAPPING.put("month", ChronoUnit.MONTHS);
        UNIT_MAPPING.put("months", ChronoUnit.MONTHS);

        // Days
        UNIT_MAPPING.put("d", ChronoUnit.DAYS);
        UNIT_MAPPING.put("day", ChronoUnit.DAYS);
        UNIT_MAPPING.put("days", ChronoUnit.DAYS);

        // Hours
        UNIT_MAPPING.put("h", ChronoUnit.HOURS);
        UNIT_MAPPING.put("hour", ChronoUnit.HOURS);
        UNIT_MAPPING.put("hours", ChronoUnit.HOURS);

        // Minutes
        UNIT_MAPPING.put("m", ChronoUnit.MINUTES);
        UNIT_MAPPING.put("minute", ChronoUnit.MINUTES);
        UNIT_MAPPING.put("minutes", ChronoUnit.MINUTES);

        // Seconds
        UNIT_MAPPING.put("s", ChronoUnit.SECONDS);
        UNIT_MAPPING.put("second", ChronoUnit.SECONDS);
        UNIT_MAPPING.put("seconds", ChronoUnit.SECONDS);
    }

    public TimeArgument(CommandArgumentBuilder<Duration> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<Duration> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public TimeArgument build() {
                return new TimeArgument(this);
            }
        };
    }

    @Override
    public boolean isTypeValid(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Check if the entire input matches expected format
        if (!TIME_PATTERN.matcher(input.trim()).matches()) {
            return false;
        }

        try {
            // Attempt to parse and validate the duration isn't zero
            Duration duration = parseDuration(input);
            return !duration.isZero();
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public Duration convert(String input) {
        if (!isTypeValid(input)) {
            throw new DateTimeParseException("Invalid duration format", input, 0);
        }
        return parseDuration(input);
    }

    @Override
    public String getErrorMessage(String input) {
        return generateErrorMessage(input,
            "valid time duration (e.g., 1day, 5hours, 10m, 1year2months, 1d12h30m)");
    }

    @Override
    protected List<String> getCustomSuggestions(CommandSender sender, String currentInput) {
        // Provide helpful suggestions based on partial input
        List<String> suggestions = new ArrayList<>();

        // If input is empty or just a number, suggest all basic units
        if (currentInput.matches("\\d*")) {
            suggestions.addAll(Arrays.asList(
                currentInput + "s",
                currentInput + "m",
                currentInput + "h",
                currentInput + "d",
                currentInput + "mo",
                currentInput + "y"
            ));
        }

        return suggestions;
    }

    private static Duration parseDuration(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be null or empty");
        }

        Period period = Period.ZERO;
        Duration duration = Duration.ZERO;

        Matcher matcher = UNIT_PATTERN.matcher(input.toLowerCase());
        boolean found = false;

        while (matcher.find()) {
            found = true;
            long value = Long.parseLong(matcher.group(1));
            String unitStr = matcher.group(2).toLowerCase();

            // Extract the base unit (first letter or two letters for month)
            String baseUnit = unitStr.startsWith("mo") ? "mo" : unitStr.substring(0, 1);

            TemporalUnit unit = UNIT_MAPPING.get(baseUnit);
            if (unit == null) {
                throw new DateTimeParseException("Invalid time unit: " + unitStr, input, matcher.start(2));
            }

            // Handle different temporal units
            if (unit == ChronoUnit.YEARS || unit == ChronoUnit.MONTHS || unit == ChronoUnit.DAYS) {
                period = period.plus(Period.of(
                    unit == ChronoUnit.YEARS ? (int)value : 0,
                    unit == ChronoUnit.MONTHS ? (int)value : 0,
                    unit == ChronoUnit.DAYS ? (int)value : 0
                ));
            } else {
                // Handle hours, minutes, seconds
                duration = duration.plus(Duration.of(value, unit));
            }
        }

        if (!found) {
            throw new DateTimeParseException("No valid time units found", input, 0);
        }

        // Combine period and duration
        return duration.plus(period.getDays(), ChronoUnit.DAYS)
            .plus(period.getMonths() * 30L, ChronoUnit.DAYS)
            .plus(period.getYears() * 365L, ChronoUnit.DAYS);
    }
}
