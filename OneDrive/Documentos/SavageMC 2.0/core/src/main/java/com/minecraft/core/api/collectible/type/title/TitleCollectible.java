package com.minecraft.core.api.collectible.type.title;

import com.minecraft.core.account.context.objects.rank.type.RankType;
import com.minecraft.core.api.collectible.Collectible;
import com.minecraft.core.api.collectible.CollectibleCategory;
import com.minecraft.core.api.collectible.rarity.CollectibleRarity;

import java.util.List;

public abstract class TitleCollectible extends Collectible {

    public TitleCollectible(String name, CollectibleRarity rarity, List<RankType> ranks, long releasedAt) {
        super(name, CollectibleCategory.TITLE, rarity, ranks, releasedAt);
    }
    
    /**
     * Retorna o título a ser exibido
     */
    public abstract String getTitle();
    
    /**
     * Retorna o subtítulo a ser exibido (pode ser null)
     */
    public String getSubtitle() {
        return null;
    }
}
