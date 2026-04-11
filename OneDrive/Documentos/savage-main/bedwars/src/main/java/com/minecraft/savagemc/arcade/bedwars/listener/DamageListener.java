package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.arcade.feature.ArcadeFeature;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageEvent;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

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
        
        // Verificar se as arenas existem
        if (user.getArena() == null || killer.getArena() == null) {
            event.setCancelled(true);
            return;
        }

        Arcade arcade = user.getArena().getArcade();
        
        if (arcade == null) {
            event.setCancelled(true);
            return;
        }

        if (damager.equals(target)) {
            event.setCancelled(true);
            return;
        }

        if (user.isProtected() || killer.isProtected()) {
            event.setCancelled(true);
            return;
        }

        if (!arcade.hasFeature(ArcadeFeature.DAMAGE) || !arcade.equals(killer.getArena().getArcade())) {
            event.setCancelled(true);
            return;
        }
        
        // Verificar se os times existem
        if (user.getTeam() == null || killer.getTeam() == null) {
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
            } else {
                user.setCombat(damager);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKnockback(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        
        Player target = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        
        User targetUser = (User) User.of(target.getUniqueId());
        User damagerUser = (User) User.of(damager.getUniqueId());
        
        if (targetUser == null || damagerUser == null) return;
        if (!targetUser.isPlayer() || !damagerUser.isPlayer()) return;
        if (!targetUser.inState(ArcadeState.ALIVE) || !damagerUser.inState(ArcadeState.ALIVE)) return;
        
        // Knockback estilo Hypixel - mais suave e balanceado
        Bukkit.getScheduler().runTaskLater(BedWars.getInstance(), () -> {
            Vector velocity = target.getVelocity();
            
            // Knockback horizontal (X e Z) - 100% do vanilla
            double horizontalMultiplier = 1.0;
            velocity.setX(velocity.getX() * horizontalMultiplier);
            velocity.setZ(velocity.getZ() * horizontalMultiplier);
            
            // Knockback vertical (Y) - reduzido para 35% (estilo Hypixel)
            // Isso mantém o jogador mais no chão e menos "flutuando"
            velocity.setY(velocity.getY() * 0.35);
            
            target.setVelocity(velocity);
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onDeadByMonster(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Creature) {
            Player player = (Player) event.getEntity();

            Creature creature = (Creature) event.getDamager();

            User user = (User) User.of(player.getUniqueId());

            if (user == null || user.isProtected() || !user.inState(ArcadeState.ALIVE)) {
                event.setCancelled(true);
                return;
            }

            Arcade arcade = user.getArcade();

            boolean dead = (player.getHealth() - event.getFinalDamage()) <= 0.5;

            if (dead) {
                event.setCancelled(true);

                if (creature.getTarget() != null && creature.getTarget().equals(player))
                    creature.setTarget(null);

                arcade.handleDeath(user.getArena(), user, null, Arcade.DeathCause.PLAYER);
            }
        }
    }
}
