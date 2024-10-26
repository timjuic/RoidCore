package me.timjuice.roidCore.commands;

import lombok.Getter;
import lombok.Setter;
import me.timjuice.roidCore.RoidCore;
import me.timjuice.roidCore.commands.arguments.Arguments;
import me.timjuice.roidCore.commands.arguments.CommandArgument;
import me.timjuice.roidCore.commands.arguments.InfiniteStringArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter
{
    private final LinkedHashMap<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final HelpCommand commandHelp;
    @Getter
    private final String baseCmdName;
    @Getter
    @Setter
    private String basePermission;
    private final String[] aliases;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final RoidCore roidPlugin;
    private final String baseDescription;

    public CommandManager(RoidCore roidPlugin, String baseCmdName, String basePermission, String baseDescription, String[] aliases) {
        this.roidPlugin = roidPlugin;
        this.baseCmdName = baseCmdName;
        this.basePermission = basePermission;
        this.baseDescription = baseDescription;
        this.aliases = (aliases != null) ? aliases : new String[0]; // Default to empty array if null

        this.registerCommand();

        this.commandHelp = new HelpCommand(roidPlugin, this);
        this.addCommand(this.commandHelp);

        // Set executor and tab completer
        if (roidPlugin.getCommand(baseCmdName) != null) {
            roidPlugin.getCommand(baseCmdName).setExecutor(this);
            roidPlugin.getCommand(baseCmdName).setTabCompleter(this);
        }
    }

    public CommandManager(RoidCore roidPlugin, String baseCmdName, String[] aliases) {
        this(roidPlugin, baseCmdName, "", "Base " + baseCmdName + " command", aliases);
    }

    public CommandManager(RoidCore roidPlugin, String baseCmdName) {
        this(roidPlugin, baseCmdName, "", "Base " + baseCmdName + " command", null);
    }

    public void registerCommand() {
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            CommandMap commandMap = (CommandMap) f.get(Bukkit.getServer());
            Command baseCommand = new Command(baseCmdName, "Base command description", "/" + baseCmdName, List.of(aliases)) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return onCommand(sender, this, label, args);  // Delegate to CommandManager's onCommand
                }

                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return onTabComplete(sender, this, alias, args);  // Delegate to CommandManager's onTabComplete
                }
            };

            // Register the command
            commandMap.register(roidPlugin.getName(), baseCommand);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] args) {
        boolean isDirectExecute = !isBaseCommand(string) && subCommandExists(string);

        // If no arguments are provided and the command is the base command
        if (args.length == 0 && command.getName().equalsIgnoreCase(this.getBaseCmdName())) {
            if (commandSender.hasPermission(commandHelp.getPermission()) || !commandHelp.requiresPermission()) {
                commandHelp.execute(commandSender, new Arguments(roidPlugin));
            } else {
                commandSender.sendMessage(roidPlugin.getMessageConfig().getNoPermissionMessage());
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
                commandSender.sendMessage(roidPlugin.getMessageConfig().getOnlyPlayersCommandMessage());
                return true;
            }

            // Check permission
            if (subcommand.requiresPermission() && !commandSender.hasPermission(subcommand.getPermission()) && !commandSender.isOp()) {
                commandSender.sendMessage(roidPlugin.getMessageConfig().getNoPermissionMessage());
                return true;
            }

            // Check argument count
            if ((args.length - argsOffset) < subcommand.getMinArgs()) {
                commandSender.sendMessage(ChatColor.RED + "Not enough args! Use: " + ChatColor.DARK_RED + String.format("/%s %s %s", getBaseCmdName(), subcommand.getName(), subcommand.getUsage()));
                return true;
            }

            // Validate and convert arguments
            Arguments arguments = validateAndConvertArguments(subcommand, Arrays.copyOfRange(args, argsOffset, args.length), commandSender);
            if (arguments == null) {
                return true; // An error message has already been sent
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
            subcommand.execute(commandSender, arguments);
            return true;
        }

        // If the command entered isn't valid
        commandSender.sendMessage(roidPlugin.getMessageConfig().getInvalidCommandMessage().replace("{PLUGIN_NAME}", roidPlugin.getName()));
        return true;
    }

    public void addCommand(SubCommand subCommand) {
        // Validate the subcommand's arguments before adding it
        validateSubCommand(subCommand);

        this.subCommands.put(subCommand.getClass().getName(), subCommand);
        if (subCommand.isRegisterDirectly()) {
            roidPlugin.getCommand(subCommand.getName()).setExecutor(this);
        }
    }

    public Collection<SubCommand> getSubCommands()
    {
        return Collections.unmodifiableCollection(subCommands.values());
    }
    public SubCommand getSubCommand(String subcommandName) {
        return subCommands.get(subcommandName);
    }

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
        if (!(sender.hasPermission(basePermission) || sender.isOp())) {
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

    private Arguments validateAndConvertArguments(SubCommand subcommand, String[] args, CommandSender sender) {
        List<CommandArgument<?>> subcommandArgs = subcommand.getArguments();
        Arguments arguments = new Arguments(roidPlugin);

        StringBuilder infiniteStringBuilder = new StringBuilder();

        for (int i = 0; i < subcommandArgs.size(); i++) {
            CommandArgument<?> arg = subcommandArgs.get(i);

            // Validate and convert argument
            if (i < args.length) {
                if (!arg.isValid(args[i])) {
                    sender.sendMessage(arg.getErrorMessage(args[i]));
                    return null;
                }

                // Check if the current argument is an InfiniteStringArgument
                if (arg instanceof InfiniteStringArgument) {
                    String[] remainingArgs = Arrays.copyOfRange(args, i, args.length);
                    infiniteStringBuilder.append(String.join(" ", remainingArgs));
                    Object convertedValue = arg.convert(infiniteStringBuilder.toString());
                    arguments.put(arg.getName(), convertedValue); // Store validated and converted argument
                } else {
                    // Convert to the specific type and store in Arguments
                    Object convertedValue = arg.convert(args[i]);
                    arguments.put(arg.getName(), convertedValue); // Store validated and converted argument
                }
            }
        }

        return arguments; // Return the populated Arguments instance
    }


    private void validateSubCommand(SubCommand subCommand) {
        List<CommandArgument<?>> arguments = subCommand.getArguments();

        boolean hasOptionalArg = false;
        boolean hasInfiniteStringArg = false;

        for (int i = 0; i < arguments.size(); i++) {
            CommandArgument<?> arg = arguments.get(i);

            // Check if the argument is optional
            if (!arg.isRequired()) {
                hasOptionalArg = true;
            } else if (hasOptionalArg) {
                // A required argument comes after an optional argument
                throw new IllegalArgumentException("Required argument '" + arg.getName() + "' cannot follow optional arguments in subcommand '" + subCommand.getName() + "'.");
            }

            // Check if the argument is an infinite string argument
            if (arg instanceof InfiniteStringArgument) {
                hasInfiniteStringArg = true;

                // Ensure it is the last argument
                if (i < arguments.size() - 1) {
                    throw new IllegalArgumentException("Infinite string argument must be the last argument in subcommand '" + subCommand.getName() + "'.");
                }
            }
        }
    }
}
