package com.minecraft.arcade.eventos.eventos.sumo;

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

public class SumoEvento implements EventoPronto, Listener {

    private final Eventos plugin;
    private Location arenaCenter;

    public SumoEvento(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "sumo";
    }

    @Override
    public String getNome() {
        return "Sumo";
    }

    @Override
    public EventoTipo getTipo() {
        return EventoTipo.SOLO;
    }

    @Override
    public Location prepararMapa(World world, Location origin) {
        arenaCenter = origin.clone();
        return SumoMapGenerator.gerar(world, origin);
    }

    @Override
    public void limparMapa() {
        if (arenaCenter == null) return;
        int r = SumoMapGenerator.getRaio();
        int cx = arenaCenter.getBlockX();
        int cy = arenaCenter.getBlockY();
        int cz = arenaCenter.getBlockZ();
        World w = arenaCenter.getWorld();
        for (int y = cy; y <= cy + 2; y++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + z * z <= r * r) {
                        w.getBlockAt(cx + x, y, cz + z).setType(Material.AIR);
                    }
                }
            }
        }
        arenaCenter = null;
    }

    @Override
    public void registerListeners(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        Player p = e.getPlayer();
        if (!plugin.getEventManager().isParticipando(p.getUniqueId())) return;
        if (arenaCenter == null) return;
        Location to = e.getTo();
        if (!SumoMapGenerator.estaNaArena(to, arenaCenter)) {
            plugin.getEventManager().jogadorMorreu(p);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!plugin.getEventManager().isParticipando(p.getUniqueId())) return;
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL || e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            plugin.getEventManager().jogadorMorreu(p);
        }
    }
}
