package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;

public class HumanTorch extends BedCollectible {

    public HumanTorch() {
        super(BedCollectibleType.FINAL_KILL, CollectibleRarity.RARE, "Tocha Humana", 
              Item.of(Material.TORCH), System.currentTimeMillis());
        
        setLore("§7Fumaça marca o local", "§7da eliminação.");
        setRanks(RankType.SAVAGE);
    }

    public void execute(Location location) {
        // Efeito de fumaça em 3 alturas
        for (int i = 0; i < 5; i++) {
            location.getWorld().playEffect(location.clone().add(
                Math.floor(Math.random() * 2.0), 0, Math.floor(Math.random() * 2.0)
            ), Effect.SMOKE, 0);
            
            location.getWorld().playEffect(location.clone().add(
                Math.floor(Math.random() * 2.0), 1, Math.floor(Math.random() * 2.0)
            ), Effect.SMOKE, 0);
            
            location.getWorld().playEffect(location.clone().add(
                Math.floor(Math.random() * 2.0), 1.5, Math.floor(Math.random() * 2.0)
            ), Effect.SMOKE, 0);
        }
    }
}
