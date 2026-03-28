package com.minecraft.savagemc.core.bukkit.api.hologram.leaderboard;

import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.bukkit.manager.list.HologramManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class LeaderboardHologram {

    private final transient HologramManager manager = BukkitCore.getManager().getHologram();

    private final String name;
    private final Player host;

    private final Location location;

    private final int maxLines;

    private HologramClient hologram;

    private long nextUpdate;

    public LeaderboardHologram(String name, Player host, Location location, int maxLines) {
        this.name = name;
        this.host = host;

        this.location = location;

        this.maxLines = maxLines;

        this.hologram = BukkitCore.getManager().getHologram().spawnClient(host, "leaderboard_" + name.toLowerCase(), location);

        this.nextUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
    }

    public LeaderboardHologram(String name, Player host, Room arena, int maxLines) {
        this.name = name;
        this.host = host;

        this.location = arena.getLocation("leaderboard_" + name.toLowerCase());

        this.maxLines = maxLines;

        this.hologram = BukkitCore.getManager().getHologram().spawnClient(host, "leaderboard_" + name.toLowerCase(), location);

        this.nextUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
    }

    public abstract void handle();

    public void update() {
        this.nextUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

        handle();
    }

    public boolean hasUpdate() {
        return nextUpdate <= System.currentTimeMillis();
    }
}
