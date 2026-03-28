package com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;

import java.util.Collections;

public class None extends Kit {

    public None() {
        super("Nenhum", Item.of(Material.BARRIER), KitStyle.NONE,
                Collections.singletonList("§7Kit sem habilidades."));
    }
}
