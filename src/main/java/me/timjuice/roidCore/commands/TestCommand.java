package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.commands.arguments.Arguments;
import me.timjuice.roidCore.commands.arguments.IntegerArgument;
import me.timjuice.roidCore.commands.arguments.PlayerArgument;
import me.timjuice.roidCore.commands.arguments.StringArgument;
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
                .addArgument(new StringArgument("dungeon", true, "dungeon1", "dungeon2", "dungeon3")) // Required string argument
                .addArgument(new PlayerArgument("receiver", true)) // Optional integer argument
                .addArgument(new IntegerArgument("amount", true)));
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        String dungeon = args.get("dungeon");
        Player receiver = args.get("receiver");
        int amount = args.get("amount");

        Bukkit.broadcastMessage(dungeon + " " + receiver.getName() + " " + amount );
    }
}
