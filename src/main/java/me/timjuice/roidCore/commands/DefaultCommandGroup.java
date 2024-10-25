package me.timjuice.roidCore.commands;

public enum DefaultCommandGroup implements CommandGroup {
    GENERAL("General"),
    ADMIN("Admin"),
    MODERATION("Moderation"),
    UTILITY("Utility"),
    PLAYER("Player"),
    NONE("None");


    private final String name;

    DefaultCommandGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
