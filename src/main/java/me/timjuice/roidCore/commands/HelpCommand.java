package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.RoidCore;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends SubCommand
{
    private final CommandManager baseCommand;

    public HelpCommand(CommandManager baseCommand)
    {
        super("help", new String[]{}, "Help command", "", "", 0, false, false);
        this.baseCommand = baseCommand;
    }

    public void execute(CommandSender sender, String[] args) {
        SubCommand[] subcommands = baseCommand.getSubCommands().toArray(new SubCommand[0]);
        List<String> helpMessageSummary = new ArrayList<>();

        for (int i = 0; i < subcommands.length; i++) {
            SubCommand command = subcommands[i];
            if (!sender.hasPermission(command.getPermission()) && !sender.isOp()) continue;
            String message = ChatColor.translateAlternateColorCodes(
                    '&',
                    String.format("&a/%s %s &f- &7%s", baseCommand.getBaseCmdName(), command.getName(), command.getDescription()));
            helpMessageSummary.add(message);
        }

        if (helpMessageSummary.isEmpty()) {
            sender.sendMessage(RoidCore.getInstance().getConfig().getNoPermissionMessage());
            return;
        }

        helpMessageSummary.add(0, RoidCore.getInstance().getConfig().getHelpMessageHeader());
        for (String message : helpMessageSummary) {
            sender.sendMessage(message);
        }
    }

}
