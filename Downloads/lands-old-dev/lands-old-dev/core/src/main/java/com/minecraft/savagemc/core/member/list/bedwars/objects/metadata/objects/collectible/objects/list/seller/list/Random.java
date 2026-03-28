package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;
import org.bukkit.Material;

public class Random extends SellerCollectible {

    public Random() {
        super(null, "Aleatório", Item.of(Material.NAME_TAG), System.currentTimeMillis());

        setLore("§7Selecione um colecionável", "§7aleatório que você possui.");
    }
}
