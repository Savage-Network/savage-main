package com.minecraft.savagemc.core.api.collectible.operator.list;

import com.minecraft.savagemc.core.api.collectible.Collectible;
import com.minecraft.savagemc.core.api.collectible.CollectibleCategory;
import com.minecraft.savagemc.core.api.collectible.nms.module.balloon.BalloonEntity;
import com.minecraft.savagemc.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.savagemc.core.api.collectible.operator.CollectibleOperator;
import com.minecraft.savagemc.core.api.collectible.type.balloon.BalloonCollectible;
import org.bukkit.entity.Player;

public class BalloonOperator extends CollectibleOperator {

    private BalloonEntity bat;
    private BalloonEntity stand;

    public BalloonOperator(Player host, BalloonCollectible balloon) {
        super(host, CollectibleCategory.BALLOON);

        this.handle(balloon);
    }

    @Override
    public void handle(Collectible collectible) {
        BalloonCollectible balloon = (BalloonCollectible) collectible;

        try {
            this.bat = NMS.getInstance().createBalloonBat(getHost());
            this.stand = NMS.getInstance().createBalloonArmorStand(getHost(), bat, balloon.getFrames());
        } catch (Exception e) {
            // NMS não disponível, ignora sem kickar
            this.bat = null;
            this.stand = null;
        }

        setCollectible(balloon);
    }

    @Override
    public void cancel() {
        if (bat != null) {
            bat.kill();
            bat = null;
        }

        if (stand != null) {
            stand.kill();
            stand = null;
        }
    }
}
