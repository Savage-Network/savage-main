package com.minecraft.savagemc.core.api.collectible.nms.v1_8_R3.module.companion.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.collectible.nms.v1_8_R3.NMS;
import com.minecraft.savagemc.core.api.collectible.nms.v1_8_R3.module.companion.CompanionSlime;
import com.minecraft.savagemc.core.api.collectible.operator.list.CompanionOperator;
import com.minecraft.savagemc.core.api.collectible.type.companion.CompanionCollectible;
import com.minecraft.savagemc.core.api.collectible.type.companion.objects.CompanionModel;
import com.minecraft.savagemc.core.api.collectible.util.math.RelativeLocation;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;

public class MiniMeEntity extends CompanionSlime {

    public MiniMeEntity(CompanionOperator companion) {
        super(companion);

        CompanionCollectible collectible = (CompanionCollectible) companion.getCollectible();

        this.maxFrames = collectible.getFrames().isEmpty() ? 0 : collectible.getFrames().get(0).size();
        this.maxIdleFrames = collectible.getIdleFrames().isEmpty() ? 0 : collectible.getIdleFrames().get(0).size();

        Account account = Savage.getAccountController().of(companion.getHost().getUniqueId());

        CompanionModel body = this.buildPart("body", new RelativeLocation(0.0, 0.0, 0.0), true);

        body.getStand().setVisible(true);
        body.getStand().setBasePlate(false);

        if (account != null)
            body.getStand().setHelmet(Item.of(Material.SKULL_ITEM, 3).skullByBase64(account.getSkin().getValue()));

        body.getStand().setChestplate(new Item(Material.LEATHER_CHESTPLATE));
        body.getStand().setLeggings(new Item(Material.LEATHER_LEGGINGS));
        body.getStand().setBoots(new Item(Material.LEATHER_BOOTS));

        this.spawn();
    }

    @Override
    public void setCompanionName(String name) {
        ((CompanionStand) NMS.getInstance().getHandle(this.models.get(0).getStand())).setCompanionName(name);
    }
}
