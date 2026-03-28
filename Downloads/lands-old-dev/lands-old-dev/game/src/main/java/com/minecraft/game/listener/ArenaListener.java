package com.minecraft.game.listener;

import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerTickEvent;

@IgnoreEvent
public class ArenaListener implements Listener {

    private int ticks = 0;

    @EventHandler
    public void onTick(ServerTickEvent event) {
        ticks++;

        if (ticks % 20 != 0) return;

        Savage.getArcadeData().getRooms().forEach(room -> {
            if (room instanceof Arena) {
                Arena arena = (Arena) room;
                arena.handlePulse();
            }
        });
    }
}
