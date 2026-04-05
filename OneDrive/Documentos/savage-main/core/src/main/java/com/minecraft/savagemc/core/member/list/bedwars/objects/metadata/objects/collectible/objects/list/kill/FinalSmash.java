package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class FinalSmash extends BedCollectible {

    public FinalSmash() {
        super(BedCollectibleType.FINAL_KILL, CollectibleRarity.MYTHICAL, "Final Smash", 
              Item.of(Material.DIAMOND_SWORD), System.currentTimeMillis());
        
        setLore("§7Um armor stand gira", "§7e explode em fogos!");
        setRanks(RankType.BETA);
    }

    public void execute(Location location) {
        Location spawnLoc = location.clone().add(0.5, 0, 0.5);
        ArmorStand armorStand = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class);
        
        armorStand.setVisible(true);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(false);
        
        // Equipar armor stand
        armorStand.getEquipment().setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (short) 3));
        armorStand.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        armorStand.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        armorStand.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        new BukkitRunnable() {
            int ticks = 0;
            double yaw = 0;
            double addX = 0.5;
            double addZ = 0.5;
            Location lastPos = spawnLoc.clone();

            @Override
            public void run() {
                // Mudar direção a cada 40 e 20 ticks
                if (ticks == 60 || ticks == 20) {
                    addX = Math.random() < 0.5 ? 0.5 : -0.5;
                    addZ = Math.random() < 0.5 ? 0.5 : -0.5;
                }

                if (ticks >= 60) {
                    armorStand.remove();
                    
                    // Lançar fogos de artifício
                    for (int i = 0; i < 3; i++) {
                        BukkitUtil.launchFirework(lastPos);
                    }
                    
                    cancel();
                    return;
                }

                Location newLoc = armorStand.getLocation().add(addX, 0.2, addZ);
                
                if (newLoc.getBlock().getType() == Material.AIR) {
                    armorStand.teleport(newLoc);
                    
                    yaw += 10;
                    armorStand.setHeadPose(new EulerAngle(0, Math.toRadians(yaw), 0));
                    
                    newLoc.getWorld().playEffect(newLoc, Effect.CLOUD, 0);
                    lastPos = newLoc;
                    ticks++;
                } else {
                    ticks = 60;
                }
            }
        }.runTaskTimer(Savage.getJavaPlugin(), 1L, 1L);
    }
}
