package me.timjuice.roidCore.commands;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    // Main constructor with cooldown
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, boolean registerDirectly, int cooldown) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.permission = permission;
        this.syntax = syntax;
        this.minArgs = minArgs;
        this.playerOnly = playerOnly;
        this.registerDirectly = registerDirectly;
        this.cooldown = cooldown;
    }

    // Overloaded constructor without cooldown (defaults to 0)
    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, boolean registerDirectly) {
        this(name, aliases, description, permission, syntax, minArgs, playerOnly, registerDirectly, 0); // Default cooldown is 0
    }

    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly) {
        this(name, aliases, description, permission, syntax, minArgs, playerOnly, false, 0);
    }

    protected SubCommand(String name, String[] aliases, String description, String permission, String syntax, int minArgs, boolean playerOnly, int cooldown) {
        this(name, aliases, description, permission, syntax, minArgs, playerOnly, false, cooldown);
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
}
