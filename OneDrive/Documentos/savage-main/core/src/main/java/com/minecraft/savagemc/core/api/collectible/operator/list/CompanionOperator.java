package com.minecraft.savagemc.core.api.collectible.operator.list;

import com.minecraft.savagemc.core.api.collectible.Collectible;
import com.minecraft.savagemc.core.api.collectible.CollectibleCategory;
import com.minecraft.savagemc.core.api.collectible.nms.module.companion.CompanionEntities;
import com.minecraft.savagemc.core.api.collectible.nms.module.companion.CompanionEntity;
import com.minecraft.savagemc.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.savagemc.core.api.collectible.type.companion.CompanionCollectible;
import org.bukkit.entity.Player;

public class CompanionOperator extends CollectibleOperator {

    private CompanionEntity entity;

    public CompanionOperator(Player host, CompanionCollectible companion) {
        super(host, CollectibleCategory.COMPANION);

        this.handle(companion);
    }

    @Override
    public void handle(Collectible collectible) {
        CompanionCollectible companion = (CompanionCollectible) collectible;

        setCollectible(companion);

        try {
            this.entity = CompanionEntities.createForType(companion.getClass(), this);
        } catch (Exception e) {
            // Companion não disponível, ignora sem kickar
            this.entity = null;
        }
    }

    @Override
    public void cancel() {
        if (entity != null)
            entity.kill();

        this.entity = null;
    }

    public boolean update() {
        if (getHost() == null || !getHost().isOnline()) {
            cancel();
            return false;
        }

        return true;
    }
}
