package me.timjuice.roidCore.commands;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.*;

@Getter
public abstract class SubCommand {
    private final String name;
    private final String[] aliases;
    private final String description;
    private final String permission;
    private final String syntax;
    private final int minArgs;
    private int maxArgs;
    private final boolean playerOnly;
    private final boolean registerDirectly;
    private final int cooldown;
    private final String group;

    // Main constructor with cooldown
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, boolean registerDirectly, int cooldown, String group) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.permission = permission;
        this.syntax = syntax;
        this.minArgs = minArgs;
        this.playerOnly = playerOnly;
        this.registerDirectly = registerDirectly;
        this.cooldown = cooldown;
        this.group = group;
    }

    protected SubCommand(Builder builder) {
        this.name = builder.name;
        this.aliases = builder.aliases.toArray(new String[0]);
        this.description = builder.description;
        this.permission = builder.permission;
        this.syntax = builder.syntax;
        this.minArgs = builder.minArgs;
        this.playerOnly = builder.playerOnly;
        this.registerDirectly = builder.registerDirectly;
        this.cooldown = builder.cooldown;
        this.group = builder.group;
    }

    // Overloaded constructor without cooldown and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, boolean registerDirectly) {
        this(name, aliases, description, permission, syntax, minArgs, playerOnly, registerDirectly, 0, DefaultCommandGroup.NONE.getName());
    }

    // Overloaded constructor without cooldown, registerDirectly, and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly) {
        this(name, aliases, description, permission, syntax, minArgs, playerOnly, false, 0, DefaultCommandGroup.NONE.getName());
    }

    // Overloaded constructor with cooldown but without registerDirectly and group (defaults to "General")
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, int cooldown) {
        this(name, aliases, description, permission, syntax, minArgs, playerOnly, false, cooldown, DefaultCommandGroup.NONE.getName());
    }

    public Set<String> getAliases() {
        return Sets.newHashSet(aliases);
    }

    public Boolean requiresPermission() {
        return !this.permission.isEmpty();
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
        private int minArgs = 0;
        private boolean playerOnly = false;
        private boolean registerDirectly = false;
        private int cooldown = 0;
        private String group = DefaultCommandGroup.NONE.getName();

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

        public Builder minArgs(int minArgs) {
            this.minArgs = minArgs;
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
    }
}
