package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.RoidCore;
import me.timjuice.roidCore.commands.arguments.Arguments;
import me.timjuice.roidCore.commands.arguments.StringArgument;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpCommand extends SubCommand {
    private final CommandManager baseCommand;
    private final RoidCore roidPlugin;

    public HelpCommand(RoidCore roidPlugin, CommandManager baseCommand) {
        super(new SubCommand.Builder("help")
                .setDescription("Help command")
                .addArgument(
                    StringArgument.builder("group")
                        .setRequired(false)
                        .setValidOptions(() -> roidPlugin.getCommandManager().getUsedCommandGroups())
                        .build()
                )
            .setGroup("help")
        );
        this.roidPlugin = roidPlugin;
        this.baseCommand = baseCommand;
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        Map<String, List<SubCommand>> groupedCommands = new HashMap<>();
        List<SubCommand> allowedCommands = new ArrayList<>();
        boolean isGroupRequested = args.has("group");
        String requestedGroup;

        // Group commands by their group attribute and filter by permission
        for (SubCommand command : baseCommand.getSubCommands()) {
            if (!sender.hasPermission(command.getPermission()) && !sender.isOp()) {
                continue;
            }
            allowedCommands.add(command);
            groupedCommands
                    .computeIfAbsent(command.getGroup(), k -> new ArrayList<>())
                    .add(command);
        }

        if (isGroupRequested) {
            requestedGroup = args.get("group");
            StringBuilder helpMsgBuilder = new StringBuilder();
            helpMsgBuilder.append(RoidCore.getInstance().getMessageConfig().getHelpCategoryHeader()
                    .replace("{CATEGORY}", requestedGroup))
                    .append("\n");

            groupedCommands.get(requestedGroup).forEach((subCommand -> {
                String message = roidPlugin.getMessageConfig().getHelpIndividualSubcommandFormat()
                        .replace("{BASE_CMD}", baseCommand.getBaseCmdName())
                        .replace("{SUB_CMD_NAME}", subCommand.getName())
                        .replace("{SUB_CMD_DESCRIPTION}", subCommand.getDescription());
                helpMsgBuilder.append(message).append("\n");
            }));

            sender.sendMessage(helpMsgBuilder.toString());
            return;
        }

        // Build help message
        StringBuilder helpMsgBuilder = new StringBuilder();
        helpMsgBuilder.append(RoidCore.getInstance().getMessageConfig().getHelpMessageHeader()
                .replace("{PLUGIN_NAME}", roidPlugin.getName()))
                .append("\n");

        if (allowedCommands.size() > 10) {
            // Display group help commands
            for (String groupName : groupedCommands.keySet()) {
                helpMsgBuilder.append(roidPlugin.getMessageConfig().getHelpCommandGroupFormat()
                        .replace("{BASE_CMD}", baseCommand.getBaseCmdName())
                        .replace("{CMD_GROUP}", groupName)
                        .replace("{CMD_GROUP}", groupName)).append("\n");
            }
        } else {
            // Display all subcommands individually
            for (SubCommand command : allowedCommands) {
                String message = roidPlugin.getMessageConfig().getHelpIndividualSubcommandFormat()
                        .replace("{BASE_CMD}", baseCommand.getBaseCmdName())
                        .replace("{SUB_CMD_NAME}", command.getName())
                        .replace("{SUB_CMD_DESCRIPTION}", command.getDescription());
                helpMsgBuilder.append(message).append("\n");
            }
        }

        sender.sendMessage(helpMsgBuilder.toString());
    }
}
