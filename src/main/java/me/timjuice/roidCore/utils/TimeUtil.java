package me.timjuice.roidCore.utils;

public class TimeUtil {
    /**
     * Converts a duration in seconds to a human-readable, short format.
     * <p>
     * The format includes only the necessary time units (e.g., "2d 5h 3m 20s")
     * and omits zero-value units. Always shows seconds, even when zero.
     * </p>
     *
     * @param totalSeconds the total duration in seconds
     * @return a formatted string representing the duration in a short, concise format
     */
    public static String formatTime(int totalSeconds) {
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append("d ");
        }
        if (hours > 0) {
            formattedTime.append(hours).append("h ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append("m ");
        }
        if (seconds > 0 || formattedTime.isEmpty()) { // Always show seconds unless time is 0
            formattedTime.append(seconds).append("s");
        }

        // Trim any trailing spaces
        return formattedTime.toString().trim();
    }

    /**
     * Converts a duration in seconds to a human-readable, long format.
     * <p>
     * This format is more verbose, with full words for time units (e.g., "2 days 5 hours 3 minutes 20 seconds")
     * and omits zero-value units. Always shows seconds, even when zero.
     * </p>
     *
     * @param totalSeconds the total duration in seconds
     * @return a formatted string representing the duration in a verbose format
     */
    public static String formatTimeLong(int totalSeconds) {
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder formattedTime = new StringBuilder();

        if (days > 0) {
            formattedTime.append(days).append(" days ");
        }
        if (hours > 0) {
            formattedTime.append(hours).append(" hours ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append(" minutes ");
        }
        if (seconds > 0 || formattedTime.isEmpty()) { // Always show seconds unless time is 0
            formattedTime.append(seconds).append(" seconds");
        }

        // Trim any trailing spaces
        return formattedTime.toString().trim();
    }

    /**
     * Converts a duration string into its equivalent total in seconds.
     * <p>
     * The input string should contain time segments in weeks (w), days (d), hours (h),
     * minutes (m), and seconds (s). Each segment consists of an integer followed by
     * one of these unit characters, e.g., "2w3d6h15m45s" for 2 weeks, 3 days, 6 hours,
     * 15 minutes, and 45 seconds.
     * </p>
     * <p>
     * If any unit is missing, it is considered as zero. For example, "1h30m" means 1 hour
     * and 30 minutes, with 0 weeks, 0 days, and 0 seconds.
     * </p>
     *
     * @param duration the input duration string containing time units and values,
     *                 in the format "2w3d6h15m45s" (case-sensitive)
     * @return the total duration in seconds as an integer
     * @throws NumberFormatException if any part of the input string is not a valid integer
     */
    public static int parseDurationToSeconds(String duration) {
        int weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0;

        // Use regex to extract both numbers and the corresponding unit (w, d, h, m, s)
        String regex = "(\\d+)([wdhms])";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(regex).matcher(duration);

        // Iterate over the matches and assign the values to the appropriate variables
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));  // Get the number
            char unit = matcher.group(2).charAt(0);          // Get the corresponding unit (w, d, h, m, s)

            switch (unit) {
                case 'w':
                    weeks = value;
                    break;
                case 'd':
                    days = value;
                    break;
                case 'h':
                    hours = value;
                    break;
                case 'm':
                    minutes = value;
                    break;
                case 's':
                    seconds = value;
                    break;
            }
        }

        // Convert everything to seconds
        int totalSeconds = 0;
        totalSeconds += weeks * 7 * 24 * 60 * 60;  // Convert weeks to seconds
        totalSeconds += days * 24 * 60 * 60;       // Convert days to seconds
        totalSeconds += hours * 60 * 60;           // Convert hours to seconds
        totalSeconds += minutes * 60;              // Convert minutes to seconds
        totalSeconds += seconds;                   // Add remaining seconds

        return totalSeconds;
    }
}
