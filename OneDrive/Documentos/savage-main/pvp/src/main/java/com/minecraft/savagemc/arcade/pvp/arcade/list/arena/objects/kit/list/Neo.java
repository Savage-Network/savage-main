package com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;

import java.util.Arrays;

public class Neo extends Kit {

    public Neo() {
        super("Neo", Item.of(Material.ARROW), KitStyle.STRATEGY, Arrays.asList(
                "§7Seja imune à projeteis", "§7e ninja."
        ));

        setRanks(RankType.VIP);
        setPrice(30000);
    }
}
