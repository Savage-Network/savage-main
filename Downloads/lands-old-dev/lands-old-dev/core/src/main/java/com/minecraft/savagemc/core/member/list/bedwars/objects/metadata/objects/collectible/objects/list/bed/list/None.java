package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list;

import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
import org.bukkit.Material;

public class None extends BedDestroyCollectible {

    public None() {
        super(null, "Nenhuma", Item.of(Material.BARRIER), System.currentTimeMillis());
    }
}
