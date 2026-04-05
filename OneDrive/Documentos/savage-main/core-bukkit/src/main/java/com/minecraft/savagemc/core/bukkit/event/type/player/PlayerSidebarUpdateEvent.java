package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.bukkit.api.sidebar.row.SidebarRow;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerSidebarUpdateEvent extends EventHandler {

    protected final Player player;
    protected final SidebarRow row;

    public PlayerSidebarUpdateEvent(Player player, SidebarRow row) {
        this.player = player;
        this.row = row;
    }
}
