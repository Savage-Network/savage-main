package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;

public class AntiTower extends Kit {

    public AntiTower(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.DIRT));
        setKitCategory(KitCategory.STRATEGY);
        setDisplayName("Anti Tower");
        setPrice(25000);
    }
}
