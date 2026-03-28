package com.minecraft.savagemc.core.bukkit.api.hologram;

import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.hologram.row.HologramRow;
import com.minecraft.savagemc.core.bukkit.api.hologram.row.animated.AnimatedRow;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.server.HologramServer;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.savagemc.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.savagemc.core.bukkit.manager.list.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HologramListener implements Listener {

    private final HologramManager manager;

    public HologramListener() {
        this.manager = BukkitCore.getManager().getHologram();
    }

    protected synchronized void updateHolograms() {
        for (Hologram hologram : manager.getHolograms().values()) {
            if (hologram == null || hologram.getLocation() == null) continue;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.inWorld(hologram.getWorld()))
                    hologram.getViewers().remove(player);

                if (hologram instanceof HologramClient) {
                    HologramClient client = (HologramClient) hologram;

                    if (client.getReceiver().equals(player) && manager.canSpawn(client, player.getLocation()))
                        client.spawnTo(player);
                    else
                        client.despawnTo(player);
                }

                if (hologram instanceof HologramServer) {
                    if (manager.canSpawn(hologram, player.getLocation()))
                        hologram.spawnTo(player);
                    else
                        hologram.despawnTo(player);
                }
            }

            if (hologram.isTemporary()) {
                hologram.updateTemporaryRow();

                if (hologram.hasExpired()) {
                    manager.getHolograms().remove(hologram.getId());

                    if (hologram instanceof HologramClient) {
                        HologramClient client = (HologramClient) hologram;

                        manager.removeClient(client);
                    }

                    if (hologram instanceof HologramServer)
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player == null || !player.isOnline()) continue;

                            hologram.despawnTo(player);
                        }
                }
            }
        }

    }

    protected synchronized void onExit(Player player) {
        for (Hologram hologram : manager.getHolograms().values()) {
            if (!(hologram instanceof HologramClient)) continue;

            HologramClient client = (HologramClient) hologram;

            if (client.getReceiver().equals(player))
                manager.removeClient(client);
        }
    }

    @EventHandler
    public void onUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            for (Hologram hologram : manager.getHolograms().values()) {
                if (hologram == null || hologram.getRows().isEmpty()) continue;

                for (HologramRow row : hologram.getRows()) {
                    if (!(row instanceof AnimatedRow)) continue;

                    AnimatedRow animated = (AnimatedRow) row;

                    animated.rotate();
                }
            }
        }

        if (event.isType(UpdateType.SECOND)) {
            updateHolograms();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        onExit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        onExit(event.getPlayer());
    }
}
