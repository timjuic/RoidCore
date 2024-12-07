package me.timjuice.roidCore.commands;

import com.google.common.collect.Sets;
import lombok.Getter;
import me.timjuice.roidCore.RoidCore;
import me.timjuice.roidCore.commands.arguments.Arguments;
import me.timjuice.roidCore.commands.arguments.CommandArgument;
import me.timjuice.roidCore.commands.arguments.InfiniteStringArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

@Getter
public abstract class SubCommand {
    private final String name;
    private final String[] aliases;
    private final String description;
    private final String permission;
    private final String syntax;
    private final boolean playerOnly;
    private final boolean registerDirectly;
    private final int cooldown;
    private final String group;
    private final List<CommandArgument<?>> arguments;
    private final Map<String, Boolean> flags;

    // Main constructor with cooldown
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, boolean playerOnly, boolean registerDirectly, int cooldown, String group, List<CommandArgument<?>> arguments, Map<String, Boolean> flags) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.permission = permission;
        this.syntax = syntax;
        this.playerOnly = playerOnly;
        this.registerDirectly = registerDirectly;
        this.cooldown = cooldown;
        this.group = group;
        this.arguments = arguments;
        this.flags = flags;
    }

    protected SubCommand(Builder builder) {
        this.name = builder.name;
        this.aliases = builder.aliases.toArray(new String[0]);
        this.description = builder.description;
        this.permission = builder.permission;
        this.syntax = builder.syntax;
        this.playerOnly = builder.playerOnly;
        this.registerDirectly = builder.registerDirectly;
        this.cooldown = builder.cooldown;
        this.group = builder.group;
        this.arguments = builder.arguments;
        this.flags = builder.flags;
    }

    // Overloaded constructor without cooldown and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, boolean playerOnly, boolean registerDirectly) {
        this(name, aliases, description, permission, syntax, playerOnly, registerDirectly, 0, DefaultCommandGroup.NONE.getName(), List.of(), new HashMap<>()); // Default to empty map for flags
    }

    // Overloaded constructor without cooldown, registerDirectly, and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, boolean playerOnly) {
        this(name, aliases, description, permission, syntax, playerOnly, false, 0, DefaultCommandGroup.NONE.getName(), List.of(), new HashMap<>()); // Default to empty map for flags
    }

    // Overloaded constructor with cooldown but without registerDirectly and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, boolean playerOnly, int cooldown) {
        this(name, aliases, description, permission, syntax, playerOnly, false, cooldown, DefaultCommandGroup.NONE.getName(), List.of(), new HashMap<>()); // Default to empty map for flags
    }

    public Set<String> getAliases() {
        return Sets.newHashSet(aliases);
    }

    public Boolean requiresPermission() {
        return !this.permission.isEmpty();
    }

    public String getUsage() {
        StringBuilder usageBuilder = new StringBuilder();

        for (CommandArgument<?> argument : this.getArguments()) {
            usageBuilder.append(argument.getUsage()).append(" ");
        }

        return usageBuilder.toString().trim();
    }

    public int getMinArgs() {
        int count = 0;
        for (CommandArgument<?> arg : arguments) {
            if (arg.isRequired()) {
                count++;
            }
        }
        return count; // Count of required arguments
    }

    // Get maximum arguments based on the total arguments defined
    public int getMaxArgs() {
        return arguments.size(); // Total number of arguments
    }

    public abstract void execute(CommandSender sender, Arguments args);

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        // Loop through the arguments defined for this subcommand
        for (int i = 0; i < arguments.size(); i++) {
            CommandArgument<?> argument = arguments.get(i);

            // If we are at the current argument position in args, suggest completions
            if (i == args.length - 1) {
                suggestions.addAll(argument.getSuggestions(sender, args[i]));
                break; // Once we find the correct position, stop the loop
            }
        }

        return suggestions;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private Set<String> aliases = new HashSet<>();
        private String description = "";
        private String permission = "";
        private String syntax = "";
        private boolean playerOnly = false;
        private boolean registerDirectly = false;
        private int cooldown = 0;
        private String group = DefaultCommandGroup.NONE.getName();
        private List<CommandArgument<?>> arguments = new ArrayList<>();
        private Map<String, Boolean> flags = new HashMap<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder setAliases(String... aliases) {
            this.aliases = new HashSet<>(Arrays.asList(aliases));
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPermission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder syntax(String syntax) {
            this.syntax = syntax;
            return this;
        }

        public Builder setPlayerOnly(boolean playerOnly) {
            this.playerOnly = playerOnly;
            return this;
        }

        public Builder setRegisterDirectly(boolean registerDirectly) {
            this.registerDirectly = registerDirectly;
            return this;
        }

        public Builder setCooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder addArgument(CommandArgument<?> argument) {
            this.arguments.add(argument); // Add argument one by one
            return this;
        }

        public Builder addFlag(String flag) {
            this.flags.put(flag, false); // Initialize the flag with false
            return this;
        }
    }

    public void executeCommand(CommandSender sender, String[] args) {
        // Validate argument count
        if (args.length < getMinArgs()) {
            sender.sendMessage(ChatColor.RED + "Not enough args! Use: " +
                ChatColor.DARK_RED + getUsage());
            return;
        }

        // Create and populate Arguments object
        Arguments arguments = new Arguments(RoidCore.getInstance());

        StringBuilder infiniteStringBuilder = new StringBuilder();
        List<String> nonFlagArgsList = new ArrayList<>();

        // First pass: process flags and collect non-flag arguments
        for (String arg : args) {
            if (arg.startsWith("-") && this.flags.containsKey(arg)) {
                arguments.setFlag(arg);
            } else {
                nonFlagArgsList.add(arg);
            }
        }

        String[] nonFlagArgs = nonFlagArgsList.toArray(new String[0]);

        // Second pass: process regular arguments
        for (int i = 0; i < this.arguments.size(); i++) {
            CommandArgument<?> commandArg = this.arguments.get(i);

            // Check if we have an argument provided
            if (i < nonFlagArgs.length) {
                String arg = nonFlagArgs[i];

                if (!commandArg.isValid(arg)) {
                    sender.sendMessage(commandArg.getErrorMessage(arg));
                    return;
                }

                if (commandArg instanceof InfiniteStringArgument) {
                    String[] remainingArgs = Arrays.copyOfRange(nonFlagArgs, i, nonFlagArgs.length);
                    infiniteStringBuilder.append(String.join(" ", remainingArgs));
                    Object convertedValue = commandArg.convert(infiniteStringBuilder.toString());
                    arguments.put(commandArg.getName(), convertedValue);
                } else {
                    Object convertedValue = commandArg.convert(arg);
                    arguments.put(commandArg.getName(), convertedValue);
                }
            } else {
                // No argument provided - check for default value
                if (!commandArg.isRequired()) {
                    Optional<?> defaultValue = commandArg.getDefaultValue();
                    defaultValue.ifPresent(o -> arguments.put(commandArg.getName(), o));
                } else {
                    // Required argument missing
                    sender.sendMessage(ChatColor.RED + "Missing required argument: " + commandArg.getName());
                    return;
                }
            }
        }

        // Execute the command with processed arguments
        execute(sender, arguments);
    }
}
