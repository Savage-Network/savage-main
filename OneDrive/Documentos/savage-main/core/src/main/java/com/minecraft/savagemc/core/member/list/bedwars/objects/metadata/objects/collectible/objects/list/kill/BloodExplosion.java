package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

public class BloodExplosion extends BedCollectible {

    public BloodExplosion() {
        super(BedCollectibleType.FINAL_KILL, CollectibleRarity.RARE, "Explosão de Sangue", 
              Item.of(Material.REDSTONE_BLOCK), System.currentTimeMillis());
        
        setLore("§7Uma explosão vermelha", "§7marca a eliminação.");
        setRanks(RankType.SAVAGE);
    }

    public void execute(Location location) {
        location.getWorld().playEffect(location.clone().add(0, 0, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
        location.getWorld().playEffect(location.clone().add(0, 2, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
    }
}
