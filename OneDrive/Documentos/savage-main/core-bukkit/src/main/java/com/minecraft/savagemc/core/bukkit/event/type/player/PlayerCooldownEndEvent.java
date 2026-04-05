package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerCooldownEndEvent extends EventHandler {

    private final Player player;
    private final Cooldown cooldown;

    public PlayerCooldownEndEvent(Player player, Cooldown cooldown) {
        this.player = player;
        this.cooldown = cooldown;
    }

    public Player getPlayer() {
        return player;
    }

    public Cooldown getCooldown() {
        return cooldown;
    }
}
