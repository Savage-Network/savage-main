/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.game.handler.listener;

import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.event.player.PlayerVanishDisableEvent;
import com.minecraft.core.bukkit.event.player.PlayerVanishEnableEvent;
import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.leaderboard.hologram.LeaderboardHologram;
import com.minecraft.core.bukkit.util.vanish.Vanish;
import com.minecraft.core.bukkit.util.variable.VariableStorage;
import com.minecraft.core.bukkit.util.variable.object.Variable;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.pattern.Condition;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import com.minecraft.savagemc.hungergames.util.leaderboard.LeaderboardPreset;
import com.minecraft.savagemc.hungergames.util.selector.Items;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;

@RecurringListener(register = GameStage.WAITING, unregister = GameStage.INVINCIBILITY)
public class WaitingListener implements Listener, BukkitInterface, Assistance, VariableStorage {

    public WaitingListener() {
        loadVariables();
    }

    // PlayerInitialSpawnEvent não existe no Spigot 1.8 - removido
    // O spawn é tratado no PlayerJoinEvent

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player p = event.getPlayer();
        Account account = Account.fetch(p.getUniqueId());
        User user = getUser(p.getUniqueId());

        p.getInventory().clear();
        p.setLevel(0);
        p.setExp(0);
        p.setHealth(20);
        p.setFoodLevel(20);
        p.getActivePotionEffects().clear();

        // Definir render distance para 4 chunks usando reflection
        try {
            // Tentar método setViewDistance se disponível
            p.getClass().getMethod("setViewDistance", int.class).invoke(p, 4);
        } catch (Exception e) {
            // Se não funcionar, tentar método alternativo
            try {
                // Usar reflection para acessar métodos internos
                Object handle = p.getClass().getMethod("getHandle").invoke(p);
                Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
                playerConnection.getClass().getMethod("a", int.class).invoke(playerConnection, 4);
            } catch (Exception ex) {
                // Se nenhum método funcionar, ignora silenciosamente
            }
        }

        user.setCondition(Condition.ALIVE);
        user.loadKits();

        if (account.hasPermission(Rank.PARTNER_PLUS))
            Vanish.getInstance().setVanished(p, account.getRank());

        Items.STARTING.apply(user);

        // Teleportar para o spawn ao entrar na fase de espera
        Location spawn = getGame().getVariables().getSpawnpoint();
        if (spawn != null)
            run(() -> p.teleport(spawn), 1L);

        for (LeaderboardPreset leaderboard : getGame().getLeaderboardPresets()) {
            LeaderboardHologram hologram = new LeaderboardHologram(leaderboard.getLeaderboard(), leaderboard.getDisplayName(), event.getPlayer(), leaderboard.getLocation());
            hologram.show();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanishEnable(PlayerVanishEnableEvent event) {
        if (!event.isCancelled()) {
            User user = getUser(event.getAccount().getUniqueId());
            user.setCondition(Condition.SPECTATOR);
            run(() -> refreshVisibility(user.getPlayer()), 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanishDisable(PlayerVanishDisableEvent event) {
        if (!event.isCancelled()) {
            User user = User.fetch(event.getAccount().getUniqueId());
            user.setCondition(Condition.ALIVE);
            getGame().handleSidebar(user);
            run(() -> refreshVisibility(user.getPlayer()), 1L);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL || !Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        HungerGames.getInstance().getUserStorage().forget(e.getPlayer().getUniqueId());
    }

    @Variable(name = "hg.pregame_build")
    private boolean build = false;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBlockBreak(BlockBreakEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()) || !build)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBlockPlace(BlockPlaceEvent e) {
        if (!Vanish.getInstance().isVanished(e.getPlayer().getUniqueId()) || !build)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

}
