package com.minecraft.savagemc.core.bukkit.listener.cosmetic;

import com.minecraft.savagemc.core.api.collectible.operator.CollectibleOperator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Carrega os collectibles ativos do jogador ao entrar no servidor.
 */
public class CosmeticSyncListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        CollectibleOperator.load(event.getPlayer());
    }
}
