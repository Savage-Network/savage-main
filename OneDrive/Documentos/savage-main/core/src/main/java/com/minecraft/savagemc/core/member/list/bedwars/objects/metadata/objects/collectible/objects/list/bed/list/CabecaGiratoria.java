package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class CabecaGiratoria extends BedDestroyCollectible {

    public CabecaGiratoria() {
        super(CollectibleRarity.RARE, "Cabeça Giratória", Item.of(Material.SKULL_ITEM, 3), 1729792699944L);

        setLore("§7Sua cabeça gira no ar!");
        setRanks(RankType.SAVAGE);
    }

    @Override
    public void execute(Location location) {
        Location spawnLoc = location.clone().add(0.5, 1, 0.5);
        ArmorStand armorStand = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class);
        
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(false);
        armorStand.setSmall(false);
        
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skull.setItemMeta(skullMeta);
        armorStand.getEquipment().setHelmet(skull);

        new BukkitRunnable() {
            int ticks = 0;
            double yaw = 0;
            Location lastPos = spawnLoc.clone();

            @Override
            public void run() {
                if (ticks >= 20) {
                    armorStand.remove();
                    location.getWorld().playEffect(lastPos, Effect.STEP_SOUND, Material.CLAY.getId());
                    cancel();
                    return;
                }

                Location newLoc = armorStand.getLocation().add(0, 0.5, 0);
                
                if (newLoc.getBlock().getType() == Material.AIR) {
                    armorStand.teleport(newLoc);
                    
                    yaw += 18;
                    armorStand.setHeadPose(new EulerAngle(0, Math.toRadians(yaw), 0));
                    
                    newLoc.getWorld().playEffect(newLoc, Effect.CLOUD, 0);
                    lastPos = newLoc;
                    ticks++;
                } else {
                    ticks = 20;
                }
            }
        }.runTaskTimer(Savage.getJavaPlugin(), 1L, 1L);
    }
}
