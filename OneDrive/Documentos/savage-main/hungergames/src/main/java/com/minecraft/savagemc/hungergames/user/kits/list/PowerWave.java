package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.particle.ParticleEffect;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PowerWave extends Kit {

    public PowerWave(HungerGames hungerGames) {
        super(hungerGames);
        setKitCategory(KitCategory.COMBAT);
        setIcon(Pattern.of(Material.INK_SACK, 13));
        setItems(new ItemFactory(Material.INK_SACK).setDurability(13).setName("§aOnda de poder").setDescription("§7Kit Power Wave").getStack());
        setPrice(35000);
        setDisplayName("Power Wave");
        setActive(false, false);
        setCooldown(30);
    }

    private final ImmutableSet<Action> ACCEPTABLES_INTERACT = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!ACCEPTABLES_INTERACT.contains(event.getAction()))
            return;

        if (!isUser(player))
            return;

        if (!isItem(player.getItemInHand()))
            return;

        if (checkInvincibility(player))
            return;

        if (isCooldown(player)) {
            dispatchCooldown(player);
            return;
        }

        addCooldown(player.getUniqueId());
        new Wave(getUser(player.getUniqueId())).start();
    }

    public static class Wave implements Assistance {

        private final User owner;
        private final Location center;

        public Wave(User owner) {
            this.owner = owner;
            this.center = owner.getPlayer().getLocation();
        }

        public void start() {

            new BukkitRunnable() {
                double t = Math.PI / 4;

                @Override
                public void run() {
                    t = t + 0.1 * Math.PI;

                    for (double theta = 0; theta <= 2 * Math.PI; theta = theta + Math.PI / 32) {

                        double x = t * Math.cos(theta);
                        double y = 2 * Math.exp(-0.1 * t) * Math.sin(t) + 1.5;
                        double z = t * Math.sin(theta);

                        center.add(x, y, z);

                        ParticleEffect.FIREWORKS_SPARK.display(center);

                        for (Entity entity : center.getChunk().getEntities()) {
                            if (entity.getLocation().distance(center) < 1.0) {
                                if (!entity.equals(owner.getPlayer())) {
                                    entity.setFireTicks(20 * 5);
                                }
                            }
                        }

                        center.subtract(x, y, z);

                        theta = theta + Math.PI / 64;

                        x = t * Math.cos(theta);

                        y = 2 * Math.exp(-0.1 * t) * Math.sin(t) + 1.5;

                        z = t * Math.sin(theta);

                        center.add(x, y, z);

                        ParticleEffect.SPELL_WITCH.display(center);

                        for (Entity entity : center.getChunk().getEntities()) {
                            if (entity.getLocation().distance(center) < 1.0) {
                                if (!entity.equals(owner.getPlayer())) {
                                    entity.setFireTicks(20 * 5);
                                }
                            }
                        }

                        center.subtract(x, y, z);
                    }

                    if (t > 10) {
                        cancel();
                    }

                }

            }.runTaskTimer(getPlugin(), 1, 1);
        }
    }

}
