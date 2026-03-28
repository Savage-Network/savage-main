package com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public class Neo extends Kit {

    public Neo() {
        super("Neo", Style.STRATEGY, Item.of(Material.ARROW), -1,
                Arrays.asList("§7Seja imune ao Anchor", "§7e Ninja."), new ArrayList<>());
    }
}
