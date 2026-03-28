package com.minecraft.savagemc.arcade.bedwars.arcade.list.versus;

import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;

public class TrioVersus extends Arcade {

    public TrioVersus(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.BEDWARS_VERSUS_TRIO);
    }
}
