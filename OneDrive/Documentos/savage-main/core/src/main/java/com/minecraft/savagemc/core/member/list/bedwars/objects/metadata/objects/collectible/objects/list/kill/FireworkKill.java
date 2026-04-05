package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;

public class FireworkKill extends BedCollectible {

    public FireworkKill() {
        super(BedCollectibleType.FINAL_KILL, CollectibleRarity.EPIC, "Fogos de Artifício", 
              Item.of(Material.FIREWORK), System.currentTimeMillis());
        
        setLore("§7Fogos coloridos celebram", "§7sua eliminação.");
        setRanks(RankType.SAVAGE);
    }

    public void execute(Location location) {
        // Lançar fogos com delay
        Savage.getPlatform().runSync(() -> {
            BukkitUtil.launchFirework(location);
        }, 8L);
    }
}
