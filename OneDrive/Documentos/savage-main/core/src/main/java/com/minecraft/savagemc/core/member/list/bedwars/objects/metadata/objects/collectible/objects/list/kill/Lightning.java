package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Material;

public class Lightning extends BedCollectible {

    public Lightning() {
        super(BedCollectibleType.FINAL_KILL, null, "Raio", Item.of(Material.BLAZE_ROD), System.currentTimeMillis());
        
        setLore("§7Um raio cairá sobre", "§7o jogador eliminado.");
    }
}
