package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.RoidCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter
{
    private final LinkedHashMap<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final HelpCommand commandHelp;
    private final String label;
    private final String[] aliases;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CommandManager(String label, String[] aliases)
    {
        this.commandHelp = new HelpCommand(this);
        this.addCommand(this.commandHelp);
        this.label = label;
        this.aliases = aliases;
        RoidCore.getInstance().getCommand(label).setExecutor(this);
        RoidCore.getInstance().getCommand(label).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
        boolean isDirectExecute = !isBaseCommand(string) && subCommandExists(string);

        // If no arguments are provided and the command is the base command
        if (args.length == 0 && command.getName().equalsIgnoreCase(this.getBaseCmdName())) {
            if (commandSender.hasPermission(commandHelp.getPermission()) || !commandHelp.requiresPermission()) {
                commandHelp.execute(commandSender, args);
            } else {
                commandSender.sendMessage(RoidCore.getInstance().getConf().getNoPermissionMessage());
            }
            return true;
        }

        // Loop through registered subcommands
        for (SubCommand subcommand : subCommands.values()) {
            // Check if the command matches the subcommand name or any of its aliases
            int argsOffset = 1;
            if (isDirectExecute) {
                // If the command is directly executed, check against subcommand name and aliases
                if (!string.equalsIgnoreCase(subcommand.getName()) && !subcommand.getAliases().contains(string.toLowerCase())) {
                    continue;
                }
                argsOffset = 0; // No need to shift arguments when directly executing
            } else {
                // If not directly executed, check against the first argument (subcommand name)
                if (!args[0].equalsIgnoreCase(subcommand.getName()) && !subcommand.getAliases().contains(args[0].toLowerCase())) {
                    continue;
                }
            }

            // Handle player-only command
            if (subcommand.isPlayerOnly() && !(commandSender instanceof Player)) {
                commandSender.sendMessage(RoidCore.getInstance().getConf().getOnlyPlayersCommandMessage());
                return true;
            }

            // Check permission
            if (subcommand.requiresPermission() && !commandSender.hasPermission(subcommand.getPermission()) && !commandSender.isOp()) {
                commandSender.sendMessage(RoidCore.getInstance().getConf().getNoPermissionMessage());
                return true;
            }

            // Check argument count
            if ((args.length - argsOffset) < subcommand.getMinArgs()) {
                commandSender.sendMessage(ChatColor.RED + "Not enough args! Use: " + ChatColor.DARK_RED + String.format("/%s %s %s", getBaseCmdName(), subcommand.getName(), subcommand.getSyntax()));
                return true;
            }

            // Handle cooldown logic
            if (commandSender instanceof Player player && subcommand.getCooldown() > 0) {
                if (isOnCooldown(player, subcommand)) {
                    String timeLeft = getCooldownTimeLeft(player, subcommand);
                    player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.WHITE + timeLeft + ChatColor.RED + " seconds before using this command again.");
                    return true;
                } else {
                    updateCooldown(player, subcommand); // Update the player's cooldown
                }
            }

            // Execute the subcommand
            subcommand.execute(commandSender, Arrays.copyOfRange(args, argsOffset, args.length));
            return true;
        }

        // If the command entered isn't valid
        commandSender.sendMessage(RoidCore.getInstance().getConf().getInvalidCommandMessage());
        return true;
    }

    public void addCommand(SubCommand subCommand)
    {
        this.subCommands.put(subCommand.getClass().getName(), subCommand);
        if (subCommand.isRegisterDirectly()) {
            RoidCore.getInstance().getCommand(subCommand.getName()).setExecutor(this);
        }
    }

    public Collection<SubCommand> getSubCommands()
    {
        return Collections.unmodifiableCollection(subCommands.values());
    }
    public SubCommand getSubCommand(String subcommandName) {
        return subCommands.get(subcommandName);
    }

    public String getBaseCmdName() { return label; }

    public Boolean subCommandExists(String searchName) {
        for (SubCommand subcommand : subCommands.values()) {
            if (subcommand.getName().equalsIgnoreCase(searchName)) return true;
            if (subcommand.getAliases().contains(searchName.toLowerCase())) return true;
        }
        return false;
    }
    public Boolean isBaseCommand(String command) {
        return Arrays.stream(this.aliases).anyMatch(baseAlias -> baseAlias.equalsIgnoreCase(command)) || command.equalsIgnoreCase(this.getBaseCmdName());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Check if the sender has permission for the base command
        if (!(sender.hasPermission("dungeons") || sender.isOp())) {
            return Collections.emptyList(); // Return no suggestions if base permission is not present
        }

        if (args.length == 0) {
            return Collections.emptyList();
        }

        // Handle subcommand tab completion
        SubCommand subcommand = subCommands.values().stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(args[0]) || cmd.getAliases().contains(args[0].toLowerCase()))
                .findFirst().orElse(null);

        if (subcommand != null) {
            // Check if sender has permission to use this subcommand
            if (subcommand.requiresPermission() && !(sender.hasPermission(subcommand.getPermission()) || sender.isOp())) {
                return Collections.emptyList(); // No suggestions if permission is missing
            }

            // Pass remaining args to the subcommand for its specific tab completion logic
            return subcommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        // Provide list of subcommands for the first argument, considering permissions
        if (args.length == 1) {
            return subCommands.values().stream()
                    .filter(cmd -> !cmd.requiresPermission() || sender.hasPermission(cmd.getPermission()) || sender.isOp()) // Filter by permissions
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private boolean isOnCooldown(Player player, SubCommand subcommand) {
        Map<String, Long> playerCooldowns = cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>());
        long lastUse = playerCooldowns.getOrDefault(subcommand.getName(), 0L);
        return (System.currentTimeMillis() - lastUse) < (subcommand.getCooldown() * 1000L);
    }

    // Get remaining cooldown time for a subcommand in seconds
    private String getCooldownTimeLeft(Player player, SubCommand subcommand) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null || !playerCooldowns.containsKey(subcommand.getName())) {
            return "0.0"; // No cooldown entry, returning 0 as a string
        }

        long lastUse = playerCooldowns.get(subcommand.getName());
        long elapsedTime = System.currentTimeMillis() - lastUse;
        double remainingTimeMillis = (subcommand.getCooldown() * 1000L) - elapsedTime;
        double remainingTimeSeconds = Math.max(remainingTimeMillis / 1000.0, 0.0); // Ensure we don't return negative values

        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(remainingTimeSeconds);
    }


    // Update the player's cooldown for a specific subcommand
    private void updateCooldown(Player player, SubCommand subcommand) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(subcommand.getName(), System.currentTimeMillis());
    }
}
