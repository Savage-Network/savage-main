package com.minecraft.core.api.collectible.type.title.list;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;
import com.minecraft.core.api.collectible.type.title.TitleCollectible;
import com.minecraft.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class ClanTitle extends TitleCollectible {

    public ClanTitle() {
        super("Clan", CollectibleRarity.MYTHICAL, Arrays.asList(RankType.EMBER_PLUS), System.currentTimeMillis());
        
        setIcon(Item.of(Material.PAPER));
        setLore(Arrays.asList(
                "§7Prévia:",
                "§eMembro de §d[11K]"
        ));
    }

    @Override
    public String getTitle() {
        return "§c§lClan";
    }

    @Override
    public String getSubtitle() {
        return "§7Prévia:\n§eMembro de §d[11K]";
    }
}
