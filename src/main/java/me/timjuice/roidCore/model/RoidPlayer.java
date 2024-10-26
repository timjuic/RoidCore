package me.timjuice.roidCore.model;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RoidPlayer {

    private transient @Nullable OfflinePlayer player;
    private final @NonNull UUID uniqueId;

    public RoidPlayer(@NonNull UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public RoidPlayer(@NonNull Player player) {
        this.player = player;
        this.uniqueId = player.getUniqueId();
    }

    public @Nullable Player getPlayer() {
        return player instanceof Player ? (Player) player : (Player) (player = Bukkit.getPlayer(uniqueId));
    }

    public @NonNull OfflinePlayer getOfflinePlayer() {
        return player != null ? player : (player = Bukkit.getOfflinePlayer(uniqueId));
    }

    public @NonNull UUID getUniqueId() {
        return uniqueId;
    }

    public RoidPlayer msg(String message) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage(message);
        }
        return this;
    }

    public RoidPlayer sendMessage(String message) {
        return msg(message);
    }

}
