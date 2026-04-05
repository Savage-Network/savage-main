package com.minecraft.savagemc.hungergames.user.celebrations.list;

import com.minecraft.core.bukkit.BukkitGame;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.celebrations.Celebration;
import com.minecraft.savagemc.hungergames.user.celebrations.pattern.CelebrationRarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Parachute extends Celebration {

    List<Chicken> chickens = new ArrayList<>();
    boolean active;

    public Parachute(HungerGames hungerGames) {
        super(hungerGames);
        setDisplayName("Paraquedas");
        setDescription("Use seu paraquedas para descer do bolo com estilo!");
        setIcon(Pattern.of(Material.LEASH));
        setRarity(CelebrationRarity.EPIC);
        setRank(Rank.Savage);
    }

    @Override
    public void onVictory(Player player) {
        Location loc = player.getLocation();

        player.teleport(loc.clone().add(0, 35, 0));
        player.setVelocity(new Vector(0, 0, 0));

        for (int i = 0; i < 20; i++) {
            Chicken chicken = (Chicken) player.getWorld().spawnEntity(player.getLocation().add(randomDouble(), 3, randomDouble()), EntityType.CHICKEN);
            chickens.add(chicken);
            chicken.setLeashHolder(player);
        }

        Bukkit.getScheduler().runTaskLater(getHungerGames(), () -> active = true, 5);

        new BukkitRunnable() {
            public void run() {
                if (active) {
                    if (!isNotOnAir(player) && player.getVelocity().getY() < -0.3)
                        player.setVelocity(player.getVelocity().add(new Vector(0, 0.1, 0)));
                    if (isNotOnAir(player)) {
                        for (Chicken chicken : chickens) {
                            chicken.setLeashHolder(null);
                            chicken.remove();
                        }
                        player.setVelocity(new Vector(0, 0.15, 0));
                        active = false;
                    }
                }
            }
        }.runTaskTimer(BukkitGame.getEngine(), 0, 1);
    }

    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (chickens.contains(event.getEntity())) {
            event.getEntity().getNearbyEntities(1, 1, 1).stream().filter(ent -> ent instanceof Item
                    && ((Item) ent).getItemStack().getType() == Material.LEASH).forEachOrdered(Entity::remove);
        }
    }

    private boolean isNotOnAir(Player p) {
        return p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR;
    }

    private double randomDouble() {
        return Math.random() < 0.5 ? ((1 - Math.random()) * (0.5 - (double) 0) + (double) 0) : (Math.random() * (0.5 - (double) 0) + (double) 0);
    }

}
