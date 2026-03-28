package com.minecraft.savagemc.arcade.aquatic.listener;

import com.minecraft.savagemc.arcade.aquatic.arcade.Arcade;
import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.arcade.feature.ArcadeFeature;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class DamageListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(PlayerDamageEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arcade arcade = user.getArena().getArcade();

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(user.isProtected());

        /* Evento não cancelado */
        if (!event.isCancelled() && event.isDead()) {
            event.setCancelled(true);

            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(user.getArena(), user, killer, Arcade.DeathCause.PLAYER);
        }
    }

    @EventHandler
    public void onVoid(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || user.getTeam() == null) return;

        Arena arena = user.getArena();
        Arcade arcade = arena.getArcade();

        Location to = event.getTo(),
                spawn = user.getTeam().getBase();

        Block self = to.getBlock().getRelative(BlockFace.SELF);

        // Caiu na água
        if (arena.isPhase(RoomPhase.PLAYING) && self.getType().equals(Material.STATIONARY_WATER) && !user.isProtected()) {
            User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

            arcade.handleDeath(arena, user, killer, Arcade.DeathCause.WATER);

            return;
        }

        if (to.getY() <= spawn.getY() - 80) {
            if (!user.isProtected()) {
                player.teleport(player.getLocation().clone().add(0, spawn.getY() + 35, 0));

                User killer = user.inCombat() ? (User) User.of(user.getCombat().getTarget().getUniqueId()) : null;

                arcade.handleDeath(arena, user, killer, Arcade.DeathCause.VOID);
            } else
                player.teleport(spawn);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamageTarget(PlayerDamageTargetEvent event) {
        Player target = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(target.getUniqueId()), killer = (User) User.of(damager.getUniqueId());

        if (user == null || killer == null) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArena().getArcade();

        if (user.equals(killer)) {
            event.setCancelled(true);
            return;
        }

        if (killer.isProtected()) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE) || !arcade.equals(killer.getArena().getArcade())) {
            event.setCancelled(true);
            return;
        }

        if (killer.getTeam().equals(user.getTeam())) {
            event.setCancelled(true);
            return;
        }

        if (!event.isCancelled()) {
            if (event.isDead()) {
                event.setCancelled(true);

                arcade.handleDeath(user.getArena(), user, killer, Arcade.DeathCause.PLAYER);
            } else
                user.setCombat(damager);
        }
    }
}
