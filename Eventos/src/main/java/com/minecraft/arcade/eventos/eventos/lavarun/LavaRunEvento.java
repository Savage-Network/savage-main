package com.minecraft.arcade.eventos.eventos.lavarun;

import com.minecraft.arcade.eventos.Eventos;
import com.minecraft.arcade.eventos.eventos.EventoPronto;
import com.minecraft.arcade.eventos.managers.EventState;
import com.minecraft.arcade.eventos.managers.EventoTipo;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class LavaRunEvento implements EventoPronto, Listener {

    private final Eventos plugin;
    private Location pistaOrigin;

    public LavaRunEvento(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "lavarun";
    }

    @Override
    public String getNome() {
        return "Lava Run";
    }

    @Override
    public EventoTipo getTipo() {
        return EventoTipo.SOLO;
    }

    @Override
    public Location prepararMapa(World world, Location origin) {
        pistaOrigin = origin.clone();
        return LavaRunMapGenerator.gerar(world, origin);
    }

    @Override
    public void limparMapa() {
        if (pistaOrigin == null) return;
        int cx = pistaOrigin.getBlockX();
        int cy = pistaOrigin.getBlockY();
        int cz = pistaOrigin.getBlockZ();
        World w = pistaOrigin.getWorld();
        for (int z = 0; z < LavaRunMapGenerator.getComprimento(); z++) {
            for (int x = -2; x <= 2; x++) {
                w.getBlockAt(cx + x, cy, cz + z).setType(Material.AIR);
                w.getBlockAt(cx + x, cy + 1, cz + z).setType(Material.AIR);
            }
        }
        pistaOrigin = null;
    }

    @Override
    public void registerListeners(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        Player p = e.getPlayer();
        if (!plugin.getEventManager().isParticipando(p.getUniqueId())) return;
        if (pistaOrigin == null) return;
        Location to = e.getTo();
        if (to == null) return;
        if (!LavaRunMapGenerator.estaNaPista(to, pistaOrigin)) return;
        Location blockUnder = to.clone().add(0, -1, 0);
        if (blockUnder.getBlock().getType() == Material.SANDSTONE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
                    blockUnder.getBlock().setType(Material.LAVA);
                }
            }.runTaskLater(plugin, 10L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!plugin.getEventManager().isParticipando(p.getUniqueId())) return;
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.LAVA || e.getCause() == EntityDamageEvent.DamageCause.FIRE
                || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            plugin.getEventManager().jogadorMorreu(p);
        }
    }
}
