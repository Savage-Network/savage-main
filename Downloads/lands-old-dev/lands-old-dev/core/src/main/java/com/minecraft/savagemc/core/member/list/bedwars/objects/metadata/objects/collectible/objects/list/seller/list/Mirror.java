package com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.list;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.seller.SellerCollectible;
import org.bukkit.Material;

public class Mirror extends SellerCollectible {

    public Mirror() {
        super(CollectibleRarity.MYTHICAL, "Espelho", Item.of(Material.THIN_GLASS), System.currentTimeMillis());

        setLore("§7As skins serão", "§7uma cópia sua!");
        setRanks(RankType.SAVAGE);
    }
}
