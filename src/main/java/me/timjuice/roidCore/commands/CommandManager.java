package me.timjuice.roidCore.commands;

import lombok.Getter;
import lombok.Setter;
import me.timjuice.roidCore.RoidCore;
import me.timjuice.roidCore.commands.arguments.Arguments;
import me.timjuice.roidCore.commands.arguments.CommandArgument;
import me.timjuice.roidCore.commands.arguments.InfiniteStringArgument;
import me.timjuice.roidCore.utils.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static me.timjuice.roidCore.utils.FormatUtil.tc;

public class CommandManager implements CommandExecutor, TabCompleter {
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
    private CommandMap bukkitCommandMap;
    private final Map<String, SubCommand> preprocessAliasMap = new HashMap<>();

    public CommandManager(RoidCore roidPlugin, String baseCmdName, String basePermission, String baseDescription, String[] aliases) {
        this.roidPlugin = roidPlugin;
        this.baseCmdName = baseCmdName;
        this.basePermission = basePermission;
        this.baseDescription = baseDescription;
        this.aliases = (aliases != null) ? aliases : new String[0]; // Default to empty array if null

        initializeCommandMap();

        registerCommand();

        this.commandHelp = new HelpCommand(roidPlugin, this);
        this.addCommand(this.commandHelp);

        // Set executor and tab completer
        if (roidPlugin.getCommand(baseCmdName) != null) {
            roidPlugin.getCommand(baseCmdName).setExecutor(this);
            roidPlugin.getCommand(baseCmdName).setTabCompleter(this);
        }

        registerPreprocessListener();
    }

    public CommandManager(RoidCore roidPlugin, String baseCmdName, String[] aliases) {
        this(roidPlugin, baseCmdName, "", "Base " + baseCmdName + " command", aliases);
    }

    public CommandManager(RoidCore roidPlugin, String baseCmdName) {
        this(roidPlugin, baseCmdName, "", "Base " + baseCmdName + " command", null);
    }

    private void registerPreprocessListener() {
        Bukkit.getPluginManager().registerEvent(
            PlayerCommandPreprocessEvent.class,
            new Listener() {},
            EventPriority.NORMAL,
            (listener, event) -> {
                if (!(event instanceof PlayerCommandPreprocessEvent e)) return;
                handlePreprocessEvent(e);
            },
            roidPlugin
        );
    }

    private void handlePreprocessEvent(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().substring(1); // Remove the '/'
        String[] parts = message.split(" ");
        if (parts.length == 0) return;

        // Combine first two parts for potential alias (e.g., "f sideclaim")
        String potentialAlias = parts.length > 1 ? parts[0] + " " + parts[1] : parts[0];
        SubCommand subCommand = preprocessAliasMap.get(potentialAlias.toLowerCase());

        if (subCommand == null) return;

        // Check permissions
        if (subCommand.requiresPermission() &&
            !event.getPlayer().hasPermission(subCommand.getPermission()) &&
            !event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(roidPlugin.getMessageConfig().getNoPermissionMessage());
            event.setCancelled(true);
            return;
        }

        // Handle player-only commands
        if (subCommand.isPlayerOnly() && !(event.getPlayer() instanceof Player)) {
            event.getPlayer().sendMessage(roidPlugin.getMessageConfig().getOnlyPlayersCommandMessage());
            event.setCancelled(true);
            return;
        }

        // Extract arguments (skip the command parts)
        String[] args = Arrays.copyOfRange(parts, 2, parts.length);

        // Validate arguments count
        if (args.length < subCommand.getMinArgs()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Not enough args! Use: " +
                ChatColor.DARK_RED + String.format("/%s %s", parts[0] + " " + parts[1], subCommand.getUsage()));
            event.setCancelled(true);
            return;
        }

        // Validate and convert arguments
        Arguments arguments = validateAndConvertArguments(subCommand, args, event.getPlayer());
        if (arguments == null) {
            event.setCancelled(true);
            return;
        }

        // Handle cooldown
        if (subCommand.getCooldown() > 0) {
            if (isOnCooldown(event.getPlayer(), subCommand)) {
                String timeLeft = getCooldownTimeLeft(event.getPlayer(), subCommand);
                event.getPlayer().sendMessage(ChatColor.RED + "You must wait " +
                    ChatColor.WHITE + timeLeft + ChatColor.RED + " seconds before using this command again.");
                event.setCancelled(true);
                return;
            }
            updateCooldown(event.getPlayer(), subCommand);
        }

        // Execute command and cancel event
        subCommand.execute(event.getPlayer(), arguments);
        event.setCancelled(true);
    }

    private void initializeCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            bukkitCommandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            ConsoleLogger.error(roidPlugin, "Failed to initialize command map");
        }
    }

    public void registerCommand() {
        if (bukkitCommandMap == null) {
            ConsoleLogger.error(roidPlugin, "Cannot register commands - command map is null");
            return;
        }

        Command baseCommand = new Command(baseCmdName, baseDescription, "/" + baseCmdName, List.of(aliases)) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return onCommand(sender, this, label, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return onTabComplete(sender, this, alias, args);
            }
        };

        bukkitCommandMap.register(roidPlugin.getName(), baseCommand);
    }

    private void registerDirectCommand(SubCommand subCommand) {
        if (bukkitCommandMap == null) {
            ConsoleLogger.error(roidPlugin, "Cannot register direct command - command map is null");
            return;
        }

        // First, try to unregister any existing command with the same name
        Command existingCommand = bukkitCommandMap.getCommand(subCommand.getName());
        if (existingCommand != null) {
            try {
                // Get the knownCommands field from SimpleCommandMap
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(bukkitCommandMap);

                // Remove the existing command and its aliases
                knownCommands.remove(existingCommand.getName());
                if (existingCommand.getAliases() != null) {
                    for (String alias : existingCommand.getAliases()) {
                        knownCommands.remove(alias);
                    }
                }

                existingCommand.unregister(bukkitCommandMap);
                ConsoleLogger.info(roidPlugin, "Unregistered existing command: " + subCommand.getName());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                ConsoleLogger.error(roidPlugin, "Failed to unregister existing command: " + e.getMessage());
                return;
            }
        }

        // Create a new command instance for direct registration
        Command command = new Command(
            subCommand.getName(),
            subCommand.getDescription(),
            "/" + subCommand.getName(),
            new ArrayList<>(subCommand.getAliases())
        ) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return onCommand(sender, this, label, args);
            }

            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return onTabComplete(sender, this, alias, args);
            }
        };

        // Register the command with Bukkit's command map
        bukkitCommandMap.register(roidPlugin.getName().toLowerCase(), command);
        ConsoleLogger.info(roidPlugin, "Registered direct command: " + subCommand.getName() +
            (existingCommand != null ? " (overridden)" : ""));
    }

    public void clearCommands() {
        if (bukkitCommandMap == null) return;

        preprocessAliasMap.clear();

//        // Unregister base command
//        bukkitCommandMap.getCommand(baseCmdName).unregister(bukkitCommandMap);

        // Unregister all subcommands
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.isRegisterDirectly()) {
                Command command = bukkitCommandMap.getCommand(subCommand.getName());
                if (command != null) {
                    command.unregister(bukkitCommandMap);
                }
            }
        }
        subCommands.clear();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        boolean isDirectExecute = !isBaseCommand(alias) && subCommandExists(alias);

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
                if (!alias.equalsIgnoreCase(subcommand.getName()) && !subcommand.getAliases().contains(alias.toLowerCase())) {
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
                boolean baseCommandUsed = isBaseCommand(alias);
                if (baseCommandUsed) { // If player executed the subcommand through the base command
                    commandSender.sendMessage(ChatColor.RED + "Not enough args! Use: " + ChatColor.DARK_RED + String.format("/%s %s %s", alias, args[0], subcommand.getUsage()));
                } else { // If player ran the subcommand directly (for registerDirectly commands)
                    commandSender.sendMessage(ChatColor.RED + "Not enough args! Use: " + ChatColor.DARK_RED + String.format("/%s %s", alias, subcommand.getUsage()));
                }

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
        boolean commandValid = isSubCommandValid(subCommand);
        if (!commandValid) return;

        this.subCommands.put(subCommand.getClass().getName(), subCommand);

        // Register preprocess aliases
        for (String alias : subCommand.getPreprocessAliases()) {
            String lowercaseAlias = alias.toLowerCase();
            if (preprocessAliasMap.containsKey(lowercaseAlias)) {
                ConsoleLogger.warning(roidPlugin, String.format(
                    "Duplicate preprocess alias '%s' for command '%s'. Skipping.",
                    alias, subCommand.getName()));
                continue;
            }
            preprocessAliasMap.put(lowercaseAlias, subCommand);
        }

        // If the command should be registered directly, register it with Bukkit
        if (subCommand.isRegisterDirectly()) {
            registerDirectCommand(subCommand);
        }
    }

    public Collection<SubCommand> getSubCommands() {
        return Collections.unmodifiableCollection(subCommands.values());
    }

    public SubCommand getSubCommandExact(String subcommandName) {
        return subCommands.get(subcommandName);
    }

    public SubCommand getSubCommand(String searchName) {
        for (SubCommand subcommand : subCommands.values()) {
            if (subcommand.getName().equalsIgnoreCase(searchName)) {
                return subcommand;
            }
            if (subcommand.getAliases().contains(searchName.toLowerCase())) {
                return subcommand;
            }
        }
        return null;
    }

    public Boolean subCommandExists(String searchName) {
        for (SubCommand subcommand : subCommands.values()) {
            if (subcommand.getName().equalsIgnoreCase(searchName)) return true;
            if (subcommand.getAliases().contains(searchName.toLowerCase())) return true;
        }
        return false;
    }

    public List<String> getUsedCommandGroups() {
        List<String> commandGroup = new ArrayList<>();
        for (SubCommand subcommand : subCommands.values()) {
            if (commandGroup.contains(subcommand.getGroup())) continue;
            commandGroup.add(subcommand.getGroup());
        }
        return commandGroup;
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

        // Allow tabcomplete if subcommand directly used without manager prefix
        SubCommand subcommand = getSubCommand(alias);
        if (subcommand != null) {
            // Check if sender has permission to use this subcommand
            if (subcommand.requiresPermission() && !(sender.hasPermission(subcommand.getPermission()) || sender.isOp())) {
                return Collections.emptyList(); // No suggestions if permission is missing
            }

            // Pass remaining args to the subcommand for its specific tab completion logic
            return subcommand.onTabComplete(sender, Arrays.copyOfRange(args, 0, args.length));
        }

        if (args.length == 0) {
            return Collections.emptyList();
        }

        // Handle subcommand tab completion
        subcommand = subCommands.values().stream()
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

        // Create a list of non-flag arguments while processing flags
        List<String> nonFlagArgsList = new ArrayList<>();

        // First pass: process flags and collect non-flag arguments
        for (String arg : args) {
            if (arg.startsWith("-") && subcommand.getFlags().containsKey(arg)) {
                arguments.setFlag(arg);
            } else {
                nonFlagArgsList.add(arg);
            }
        }

        // Convert list back to array for further processing
        String[] nonFlagArgs = nonFlagArgsList.toArray(new String[0]);

        // Second pass: process regular arguments and handle default values
        for (int i = 0; i < subcommandArgs.size(); i++) {
            CommandArgument<?> commandArg = subcommandArgs.get(i);

//            // Check argument permission if provided
//            if (commandArg.requiresPermission() && !sender.hasPermission(commandArg.getPermission()) && !sender.isOp()) {
//                sender.sendMessage(roidPlugin.getMessageConfig().getNoPermissionMessage());
//                return null;
//            }

            // Check if we have an argument provided
            if (i < nonFlagArgs.length) {
                String arg = nonFlagArgs[i];

                // Check if player has permission to use this argument
                if (commandArg.requiresPermission() && !sender.hasPermission(commandArg.getPermission()) && !sender.isOp()) {
                    sender.sendMessage(tc(String.format("&cYou don't have permission to use argument '%s' in this command", commandArg.getName())));
                    return null;
              }

                if (!commandArg.isValid(arg)) {
                    sender.sendMessage(commandArg.getErrorMessage(arg));
                    return null;
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
                    return null;
                }
            }
        }

        return arguments;
    }


    private boolean isSubCommandValid(SubCommand subCommand) {
        List<CommandArgument<?>> arguments = subCommand.getArguments();

        boolean hasOptionalArg = false;

        // Check if the subcommand name or any of its aliases already exist
        if (subCommandExists(subCommand.getName())) {
            ConsoleLogger.error(roidPlugin, String.format(
                "Subcommand name '%s' already exists. Skipping command '%s'",
                subCommand.getName(), subCommand.getName()));
            return false;
        }
        for (String alias : subCommand.getAliases()) {
            if (subCommandExists(alias)) {
                ConsoleLogger.error(roidPlugin, String.format(
                    "Subcommand alias '%s' already exists.",
                    alias));
                return false;
            }
        }

        for (int i = 0; i < arguments.size(); i++) {
            CommandArgument<?> arg = arguments.get(i);

            // Check if the argument is optional
            if (!arg.isRequired()) {
                hasOptionalArg = true;
            } else if (hasOptionalArg) {
                // A required argument comes after an optional argument
                ConsoleLogger.error(roidPlugin, String.format(
                    "Required argument '%s' cannot follow optional arguments in subcommand '%s'.",
                    arg.getName(), subCommand.getName()));
                return false;
            }

            // Check if the argument is an infinite string argument
            if (arg instanceof InfiniteStringArgument) {
                // Ensure it is the last argument
                if (i < arguments.size() - 1) {
                    ConsoleLogger.error(roidPlugin, String.format(
                        "Infinite string argument must be the last argument in subcommand '%s'.",
                        subCommand.getName()));
                    return false;
                }
            }
        }

        return true;
    }
}
