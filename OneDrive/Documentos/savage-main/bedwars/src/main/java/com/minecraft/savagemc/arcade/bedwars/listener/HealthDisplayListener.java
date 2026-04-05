package com.minecraft.savagemc.arcade.bedwars.listener;

import com.minecraft.savagemc.arcade.bedwars.BedWars;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.state.ArcadeState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class HealthDisplayListener implements Listener {

    public HealthDisplayListener() {
        // Iniciar task que atualiza a vida a cada tick
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllHealthDisplays();
            }
        }.runTaskTimer(BedWars.getInstance(), 0L, 1L);
    }

    private void updateAllHealthDisplays() {
        for (Arena arena : BedWars.getInstance().getManager().getArcade().getArcades().stream()
                .flatMap(arcade -> arcade.getRooms().stream())
                .map(room -> (Arena) room)
                .filter(arena -> arena.isPhase(RoomPhase.PLAYING))
                .toArray(Arena[]::new)) {
            
            updateArenaHealthDisplays(arena);
        }
    }

    private void updateArenaHealthDisplays(Arena arena) {
        for (User user : arena.getAliveUsers()) {
            Player player = user.getPlayer();
            if (player == null || !player.isOnline()) continue;
            
            updatePlayerHealthDisplay(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        Savage.getPlatform().runSync(() -> updatePlayerHealthDisplay(player), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        Savage.getPlatform().runSync(() -> updatePlayerHealthDisplay(player), 1L);
    }

    private void updatePlayerHealthDisplay(Player player) {
        User user = (User) User.of(player.getUniqueId());
        
        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            return;
        }
        
        Arena arena = user.getArena();
        if (arena == null || !arena.isPhase(RoomPhase.PLAYING)) {
            return;
        }
        
        int health = (int) Math.ceil(player.getHealth());
        
        // Atualizar para todos os jogadores na arena
        for (User viewer : arena.getUsers()) {
            Player viewerPlayer = viewer.getPlayer();
            if (viewerPlayer == null || !viewerPlayer.isOnline()) continue;
            
            updateHealthBelowName(viewerPlayer, player, health);
        }
    }

    private void updateHealthBelowName(Player viewer, Player target, int health) {
        Scoreboard scoreboard = viewer.getScoreboard();
        if (scoreboard == null) return;
        
        // Criar ou obter o objective para mostrar vida abaixo do nome
        Objective objective = scoreboard.getObjective("healthBelowName");
        
        if (objective == null) {
            objective = scoreboard.registerNewObjective("healthBelowName", "health");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName("§c❤");
        }
        
        // Atualizar o score (vida) do jogador
        objective.getScore(target.getName()).setScore(health);
    }
}
