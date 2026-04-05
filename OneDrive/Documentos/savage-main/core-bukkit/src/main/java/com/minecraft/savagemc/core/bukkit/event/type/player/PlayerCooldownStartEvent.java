package com.minecraft.savagemc.core.bukkit.event.type.player;

import com.minecraft.savagemc.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.savagemc.core.bukkit.event.EventHandler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
public class PlayerCooldownStartEvent extends EventHandler implements Cancellable {

    private final Player player;
    private final Cooldown cooldown;

    private boolean cancelled;

    public PlayerCooldownStartEvent(Player player, Cooldown cooldown) {
        this.player = player;
        this.cooldown = cooldown;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
