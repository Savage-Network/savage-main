package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerArenaWarpEvent extends EventHandler {

    private Player player;
    private Room arena;

    public PlayerArenaWarpEvent() {
        this.player = null;
        this.arena = null;
    }

    public PlayerArenaWarpEvent(Player player, Room arena) {
        this.player = player;
        this.arena = arena;
    }
}
