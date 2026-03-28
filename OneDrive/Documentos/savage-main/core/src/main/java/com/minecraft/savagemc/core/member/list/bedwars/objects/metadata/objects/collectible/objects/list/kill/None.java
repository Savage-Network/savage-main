package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.kill;

import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.BedCollectible;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import org.bukkit.Material;

public class None extends BedCollectible {

    public None() {
        super(BedCollectibleType.FINAL_KILL, null, "Nenhuma", Item.of(Material.BARRIER), System.currentTimeMillis());
    }
}
