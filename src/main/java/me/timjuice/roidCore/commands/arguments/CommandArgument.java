package me.timjuice.roidCore.commands.arguments;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

@Getter
public abstract class CommandArgument<T> {
    private final String name;
    private final boolean required;
    protected final Supplier<List<String>> validOptionsSupplier;
    protected final Supplier<List<String>> suggestedOptionsSupplier;

    protected CommandArgument(CommandArgumentBuilder<T> builder) {
        this.name = builder.name;
        this.required = builder.required;
        this.validOptionsSupplier = builder.validOptionsSupplier;
        this.suggestedOptionsSupplier = builder.suggestedOptionsSupplier;
    }

    // Get current valid options
    protected List<String> getValidOptions() {
        return validOptionsSupplier != null ? validOptionsSupplier.get() : Collections.emptyList();
    }

    // Get current suggested options
    protected List<String> getSuggestedOptions() {
        return suggestedOptionsSupplier != null ? suggestedOptionsSupplier.get() : Collections.emptyList();
    }

    public static class CommandArgumentBuilder<T> {
        private final String name;
        private boolean required = true;
        private Supplier<List<String>> validOptionsSupplier = Collections::emptyList;
        private Supplier<List<String>> suggestedOptionsSupplier = Collections::emptyList;

        public CommandArgumentBuilder(String name) {
            this.name = name;
        }

        public CommandArgumentBuilder<T> setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public CommandArgumentBuilder<T> setValidOptions(Supplier<List<String>> optionsSupplier) {
            this.validOptionsSupplier = optionsSupplier;
            return this;
        }

        public CommandArgumentBuilder<T> setSuggestedOptions(Supplier<List<String>> optionsSupplier) {
            this.suggestedOptionsSupplier = optionsSupplier;
            return this;
        }
    }

    public boolean isValid(String input) {
        List<String> validOptions = getValidOptions();
        if (validOptions.isEmpty()) {
            return true;
        }
        return validOptions.contains(input);
    }

    protected abstract boolean isTypeValid(String input);
    public abstract T convert(String input);
    public abstract String getErrorMessage(String input);

    protected String generateErrorMessage(String input, String expected) {
        return tc(String.format("&cInvalid value for argument '%s'. Got: '%s'. Expected: %s.", getName(), input, expected));
    }

    public List<String> getSuggestions(CommandSender sender, String currentInput) {
        List<String> validOptions = getValidOptions();
        if (!validOptions.isEmpty()) {
            return validOptions.stream()
                .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList());
        }

        List<String> suggestedOptions = getSuggestedOptions();
        if (!suggestedOptions.isEmpty()) {
            return suggestedOptions.stream()
                .filter(option -> option.toLowerCase().startsWith(currentInput.toLowerCase()))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public String getUsage() {
        return required ? "<" + name + ">" : "[" + name + "]";
    }
}
