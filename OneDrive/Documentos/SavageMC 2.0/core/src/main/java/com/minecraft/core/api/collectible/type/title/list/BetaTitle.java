package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class BetaTitle extends TitleCollectible {

    public BetaTitle() {
        super("Desde o Início", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.BETA), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§82024",
                "",
                "§1Mostre o quanto você é antigo",
                "",
                "§7Exclusivo para §1§lBETAs"
        ));
    }

    @Override
    public String getTitle() {
        return "§1Desde o Início";
    }

    @Override
    public String getSubtitle() {
        return null;
    }
}
