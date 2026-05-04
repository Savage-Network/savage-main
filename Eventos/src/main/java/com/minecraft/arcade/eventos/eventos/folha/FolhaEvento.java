package com.minecraft.arcade.eventos.eventos.folha;

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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class FolhaEvento implements EventoPronto, Listener {

    private final Eventos plugin;
    private Location arenaCenter;

    public FolhaEvento(Eventos plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "folha";
    }

    @Override
    public String getNome() {
        return "Folha";
    }

    @Override
    public EventoTipo getTipo() {
        return EventoTipo.SOLO;
    }

    @Override
    public Location prepararMapa(World world, Location origin) {
        arenaCenter = origin.clone();
        return FolhaMapGenerator.gerar(world, origin);
    }

    @Override
    public void limparMapa() {
        if (arenaCenter == null) return;
        int r = FolhaMapGenerator.getRaio();
        int cx = arenaCenter.getBlockX();
        int cy = arenaCenter.getBlockY();
        int cz = arenaCenter.getBlockZ();
        World w = arenaCenter.getWorld();
        for (int y = cy; y <= cy + 1; y++) {
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
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.getEventManager().getState() != EventState.EM_JOGO) return;
        if (!plugin.getEventManager().isParticipando(e.getPlayer().getUniqueId())) return;
        if (arenaCenter == null) return;
        if (!FolhaMapGenerator.estaNaArena(e.getBlock().getLocation(), arenaCenter)) return;
        if (e.getBlock().getType() == Material.WOOD) {
            e.setCancelled(true);
            return;
        }
        if (e.getBlock().getType() == Material.LEAVES || e.getBlock().getType() == Material.LEAVES_2) {
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
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
