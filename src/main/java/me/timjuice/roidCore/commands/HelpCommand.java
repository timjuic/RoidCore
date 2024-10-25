package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.RoidCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpCommand extends SubCommand {
    private final CommandManager baseCommand;

    public HelpCommand(CommandManager baseCommand) {
//        super("help", new String[]{}, "Help command", "", "", 0, false, false, "General");
        super(new SubCommand.Builder("help")
                .description("Help command")
        );
        this.baseCommand = baseCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Map<String, List<SubCommand>> groupedCommands = new HashMap<>();

        // Group commands by their group attribute
        for (SubCommand command : baseCommand.getSubCommands()) {
            if (!sender.hasPermission(command.getPermission()) && !sender.isOp()) {
                continue;
            }
            groupedCommands
                    .computeIfAbsent(command.getGroup(), k -> new ArrayList<>())
                    .add(command);
        }

        // Build help message grouped by each group
        List<String> helpMessageSummary = new ArrayList<>();
        helpMessageSummary.add(RoidCore.getInstance().getConf().getHelpMessageHeader());

        for (Map.Entry<String, List<SubCommand>> entry : groupedCommands.entrySet()) {
            String groupName = entry.getKey();
            helpMessageSummary.add(ChatColor.BOLD + groupName + ":");

            for (SubCommand command : entry.getValue()) {
                String message = ChatColor.translateAlternateColorCodes('&',
                        String.format("&a/%s %s &f- &7%s", baseCommand.getBaseCmdName(), command.getName(), command.getDescription()));
                helpMessageSummary.add(message);
            }
            helpMessageSummary.add("");  // Blank line between groups
        }

        // Send the messages to the command sender
        for (String message : helpMessageSummary) {
            sender.sendMessage(message);
        }
    }
}
