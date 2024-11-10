package me.timjuice.roidCore.commands.arguments;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

/**
 * An abstract base class for command arguments that can be used in a command system.
 * This class provides functionality for argument validation, suggestion generation,
 * and value conversion.
 *
 * @param <T> The type that this argument will be converted to
 */
@Getter
public abstract class CommandArgument<T> {
    private final String name;
    private final boolean required;
    protected final Supplier<List<String>> validOptionsSupplier;
    protected final Supplier<List<String>> suggestedOptionsSupplier;
    private final T defaultValue;
    private final boolean hasDefaultValue;

    /**
     * Protected constructor for creating a new CommandArgument.
     * Instances should be created using the corresponding Builder class.
     *
     * @param builder The builder containing the configuration for this argument
     */
    protected CommandArgument(CommandArgumentBuilder<T> builder) {
        this.name = builder.name;
        this.required = builder.required;
        this.validOptionsSupplier = builder.validOptionsSupplier;
        this.suggestedOptionsSupplier = builder.suggestedOptionsSupplier;
        this.defaultValue = builder.defaultValue;
        this.hasDefaultValue = builder.hasDefaultValue;

        // Validate default value if one is set
        if (hasDefaultValue) {
            if (required) {
                throw new IllegalArgumentException("Required arguments cannot have default values");
            }

            String defaultValueStr = String.valueOf(defaultValue);
            if (!isTypeValid(defaultValueStr)) {
                throw new IllegalArgumentException(
                    String.format("Invalid default value '%s' for argument '%s'", defaultValueStr, name)
                );
            }
        }
    }

    /**
     * Retrieves the current list of valid options for this argument.
     *
     * @return A list of valid options, or an empty list if no valid options are defined
     */
    protected List<String> getValidOptions() {
        return validOptionsSupplier != null ? validOptionsSupplier.get() : Collections.emptyList();
    }

    /**
     * Retrieves the current list of suggested options for this argument.
     *
     * @return A list of suggested options, or an empty list if no suggestions are defined
     */
    protected List<String> getSuggestedOptions() {
        return suggestedOptionsSupplier != null ? suggestedOptionsSupplier.get() : Collections.emptyList();
    }

    /**
     * Abstract builder class for creating CommandArgument instances.
     * Provides a fluent interface for configuring argument properties.
     *
     * @param <T> The type that the argument will be converted to
     */
    public abstract static class CommandArgumentBuilder<T> {
        private final String name;
        private boolean required = true;
        private Supplier<List<String>> validOptionsSupplier = Collections::emptyList;
        private Supplier<List<String>> suggestedOptionsSupplier = Collections::emptyList;
        private T defaultValue;
        private boolean hasDefaultValue = false;

        /**
         * Creates a new builder for a CommandArgument with the specified name.
         *
         * @param name The name of the argument
         */
        protected CommandArgumentBuilder(String name) {
            this.name = name;
        }

        /**
         * Sets whether this argument is required.
         *
         * @param required true if the argument is required, false otherwise
         * @return this builder for method chaining
         */
        public CommandArgumentBuilder<T> setRequired(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Sets the supplier for valid options for this argument.
         * Valid options are strictly enforced during validation.
         *
         * @param optionsSupplier A supplier that provides a list of valid options
         * @return this builder for method chaining
         */
        public CommandArgumentBuilder<T> setValidOptions(Supplier<List<String>> optionsSupplier) {
            this.validOptionsSupplier = optionsSupplier;
            return this;
        }

        /**
         * Sets the supplier for suggested options for this argument.
         * Suggested options are used for tab completion but not enforced during validation.
         *
         * @param optionsSupplier A supplier that provides a list of suggested options
         * @return this builder for method chaining
         */
        public CommandArgumentBuilder<T> setSuggestedOptions(Supplier<List<String>> optionsSupplier) {
            this.suggestedOptionsSupplier = optionsSupplier;
            return this;
        }

        public CommandArgumentBuilder<T> setDefaultValue(T defaultValue) {
            if (required) {
                throw new IllegalArgumentException("Cannot set default value for required argument");
            }
            this.defaultValue = defaultValue;
            this.hasDefaultValue = true;
            return this;
        }

        /**
         * Builds a new CommandArgument instance with the configured properties.
         *
         * @return A new CommandArgument instance
         */
        public abstract CommandArgument<T> build();
    }

    /**
     * Validates whether the provided input is valid for this argument.
     * Checks both the valid options list (if any) and type-specific validation.
     *
     * @param input The input string to validate
     * @return true if the input is valid, false otherwise
     */
    public boolean isValid(String input) {
        List<String> validOptions = getValidOptions();
        if (validOptions.isEmpty()) {
            return isTypeValid(input);
        }
        return validOptions.contains(input) && isTypeValid(input);
    }

    /**
     * Validates whether the input string is valid for this argument's specific type.
     * Must be implemented by subclasses to provide type-specific validation.
     *
     * @param input The input string to validate
     * @return true if the input is valid for this type, false otherwise
     */
    protected abstract boolean isTypeValid(String input);

    /**
     * Converts the input string to the target type T.
     * Must be implemented by subclasses to provide type-specific conversion.
     *
     * @param input The input string to convert
     * @return The converted value of type T
     */
    public abstract T convert(String input);

    /**
     * Gets an error message for when the input is invalid.
     * Must be implemented by subclasses to provide type-specific error messages.
     *
     * @param input The invalid input
     * @return A formatted error message
     */
    public abstract String getErrorMessage(String input);

    /**
     * Gets custom suggestions specific to this argument type.
     * Override this method in subclasses to provide type-specific suggestions.
     *
     * @param sender The command sender
     * @param currentInput The current input string
     * @return List of custom suggestions, or empty list if none
     */
    protected List<String> getCustomSuggestions(CommandSender sender, String currentInput) {
        return Collections.emptyList();
    }

    /**
     * Generates a standard error message format for invalid inputs.
     *
     * @param input The invalid input
     * @param expected Description of what was expected
     * @return A formatted error message
     */
    protected String generateErrorMessage(String input, String expected) {
        return tc(String.format("&cInvalid value for argument '%s'. Got: '%s'. Expected: %s.", getName(), input, expected));
    }

    /**
     * Gets suggestions for the current input based on various sources in priority order:
     * 1. Valid options (if any)
     * 2. Builder-provided suggested options
     * 3. Custom type-specific suggestions from subclasses
     *
     * @param sender The command sender
     * @param currentInput The current input string
     * @return A filtered list of suggestions that match the current input
     */
    public final List<String> getSuggestions(CommandSender sender, String currentInput) {
        List<String> validOptions = getValidOptions();
        if (!validOptions.isEmpty()) {
            return filterSuggestions(validOptions, currentInput);
        }

        List<String> suggestedOptions = getSuggestedOptions();
        if (!suggestedOptions.isEmpty()) {
            return filterSuggestions(suggestedOptions, currentInput);
        }

        List<String> customSuggestions = getCustomSuggestions(sender, currentInput);
        return filterSuggestions(customSuggestions, currentInput);
    }

    /**
     * Filters a list of suggestions based on the current input prefix.
     *
     * @param suggestions The list of suggestions to filter
     * @param currentInput The current input to filter by
     * @return A filtered list of suggestions that start with the current input
     */
    private List<String> filterSuggestions(List<String> suggestions, String currentInput) {
        return suggestions.stream()
            .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase()))
            .collect(Collectors.toList());
    }

    /**
     * Gets the usage syntax for this argument.
     * Required arguments are enclosed in angle brackets (<>),
     * while optional arguments are enclosed in square brackets ([]).
     *
     * @return The usage syntax string for this argument
     */
    public String getUsage() {
        return required ? "<" + name + ">" : "[" + name + "]";
    }

    /**
     * Gets the default value if one is set
     *
     * @return Optional containing the default value, or empty if no default is set
     */
    public Optional<T> getDefaultValue() {
        return hasDefaultValue ? Optional.of(defaultValue) : Optional.empty();
    }
}
