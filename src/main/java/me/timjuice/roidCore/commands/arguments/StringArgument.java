package me.timjuice.roidCore.commands.arguments;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class StringArgument extends CommandArgument<String> {
    public StringArgument(CommandArgumentBuilder<String> builder) {
        super(builder);
    }

    public static CommandArgumentBuilder<String> builder(String name) {
        return new CommandArgumentBuilder<>(name) {
            @Override
            public StringArgument build() {
                return new StringArgument(this);
            }
        };
    }

    @Override
    protected boolean isTypeValid(String input) {
        return input != null && !input.trim().isEmpty();
    }

    @Override
    public String convert(String input) {
        return input;
    }

    @Override
    public String getErrorMessage(String input) {
        if (getValidOptions().isEmpty()) {
            return tc(String.format("&cInvalid argument: '%s'", input));
        }
        return tc(String.format("&cInvalid argument: '%s'. Expected: %s", input, String.join(", ", getValidOptions())));
    }
}
