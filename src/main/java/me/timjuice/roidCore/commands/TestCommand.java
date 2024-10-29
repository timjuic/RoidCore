package me.timjuice.roidCore.commands;

import me.timjuice.roidCore.commands.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class TestCommand extends SubCommand {
    public TestCommand() {
        super(new SubCommand.Builder("test")
                .setAliases("testcmd", "testing", "test123")
                .setPermission("testperm")
                .setDescription("Test command")
                .setPlayerOnly(false)
                .setCooldown(5)
                .setGroup(DefaultCommandGroup.UTILITY.getName())
                .addArgument(StringArgument.builder("dungeon").setValidOptions(() -> List.of("dungeon1", "dungeon2")).build())
                .addArgument(PlayerArgument.builder("player").setSuggestedOptions(() -> List.of("timjuice")).build())
                .addArgument(IntegerArgument.builder("amount").setSuggestedOptions(() -> List.of("1", "2", "3", "10")).build())
                .addArgument(BooleanArgument.builder("enabled").build())
                .addArgument(InfiniteStringArgument.builder("description").build())
                .addFlag("-s")
        );
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        String dungeon = args.get("dungeon");
        Player receiver = args.get("player");
        int amount = args.get("amount");
        boolean hasFlag = args.hasFlag("-s");

        if (args.has("description")) {
            String description = args.get("description");
            sender.sendMessage(dungeon + " " + receiver.getName() + " " + amount + " " + description + " " + hasFlag);
        } else {
            sender.sendMessage(dungeon + " " + receiver.getName() + " " + amount + " " + hasFlag);
        }
    }
}
