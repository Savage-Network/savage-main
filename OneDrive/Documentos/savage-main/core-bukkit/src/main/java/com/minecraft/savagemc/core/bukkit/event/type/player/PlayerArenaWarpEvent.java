package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.arcade.room.Room;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class PlayerArenaWarpEvent extends EventHandler {

    private final Player player;
    private final Room arena;
}
