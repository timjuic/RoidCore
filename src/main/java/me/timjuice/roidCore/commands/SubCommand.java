package me.timjuice.roidCore.commands;

import com.google.common.collect.Sets;
import lombok.Getter;
import me.timjuice.roidCore.commands.arguments.CommandArgument;
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
    private final CommandArgument<?>[] arguments; // Add arguments array

    // Main constructor with cooldown
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, boolean playerOnly, boolean registerDirectly, int cooldown, String group, CommandArgument<?>[] arguments) {
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
        this.arguments = builder.arguments.toArray(new CommandArgument[0]);
    }

    // Overloaded constructor without cooldown and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, boolean registerDirectly) {
        this(name, aliases, description, permission, syntax, playerOnly, registerDirectly, 0, DefaultCommandGroup.NONE.getName(), new CommandArgument[0]); // Default to empty array
    }

    // Overloaded constructor without cooldown, registerDirectly, and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly) {
        this(name, aliases, description, permission, syntax, playerOnly, false, 0, DefaultCommandGroup.NONE.getName(), new CommandArgument[0]); // Default to empty array
    }

    // Overloaded constructor with cooldown but without registerDirectly and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, int cooldown) {
        this(name, aliases, description, permission, syntax, playerOnly, false, cooldown, DefaultCommandGroup.NONE.getName(), new CommandArgument[0]); // Default to empty array
    }

    public Set<String> getAliases() {
        return Sets.newHashSet(aliases);
    }

    public Boolean requiresPermission() {
        return !this.permission.isEmpty();
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
        return arguments.length; // Total number of arguments
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();  // Default behavior: no tab suggestions
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

        public Builder(String name) {
            this.name = name;
        }

        public Builder aliases(String... aliases) {
            this.aliases = new HashSet<>(Arrays.asList(aliases));
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder syntax(String syntax) {
            this.syntax = syntax;
            return this;
        }

        public Builder playerOnly(boolean playerOnly) {
            this.playerOnly = playerOnly;
            return this;
        }

        public Builder registerDirectly(boolean registerDirectly) {
            this.registerDirectly = registerDirectly;
            return this;
        }

        public Builder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder addArgument(CommandArgument<?> argument) {
            this.arguments.add(argument); // Add argument one by one
            return this;
        }
    }
}
