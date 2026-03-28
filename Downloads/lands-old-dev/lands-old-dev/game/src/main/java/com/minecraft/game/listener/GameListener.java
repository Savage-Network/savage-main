package com.minecraft.game.listener;

import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.game.arcade.list.SumoArcade;
import com.minecraft.game.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@IgnoreEvent
public class GameListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer()) return;

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) return;

        if (player.getLocation().getY() < 50) {
            User killer = (User) user.getCombat();

            SumoArcade arcade = (SumoArcade) arena.getArcade();

            arcade.handleDeath(user, killer);

            Savage.getPlatform().runSync(() -> {
                player.teleport(arena.getLocation("spawn"));
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (!user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        User user = (User) User.of(player.getUniqueId());
        User killer = (User) User.of(damager.getUniqueId());

        if (user == null || killer == null) return;

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        user.setCombat(killer);
    }
}
