package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.commands.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TestCommand extends SubCommand {
    public TestCommand() {
        super(new SubCommand.Builder("test")
                .setAliases("testcmd", "testing", "test123")
                .setPermission("testperm")
                .setDescription("Test command")
                .setPlayerOnly(false)
                .setCooldown(5)
                .setGroup(DefaultCommandGroup.UTILITY.getName())
                .addArgument(new StringArgument("dungeon", true, "dungeon1", "dungeon2", "dungeon3")) // Required string argument
                .addArgument(new PlayerArgument("receiver", true)) // Optional integer argument
                .addArgument(new IntegerArgument("amount", true))
                .addArgument(new BooleanArgument("enabled", true))
                .addArgument(new InfiniteStringArgument("description", false))
                .addFlag("-s")
        );
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        String dungeon = args.get("dungeon");
        Player receiver = args.get("receiver");
        int amount = args.get("amount");
        boolean hasFlag = args.hasFlag("-s");

        if (args.has("description")) {
            String[] description = args.get("description");
            sender.sendMessage(dungeon + " " + receiver.getName() + " " + amount + " " + String.join(" ", description) + " " + hasFlag);
        } else {
            sender.sendMessage(dungeon + " " + receiver.getName() + " " + amount + " " + hasFlag);
        }
    }
}
