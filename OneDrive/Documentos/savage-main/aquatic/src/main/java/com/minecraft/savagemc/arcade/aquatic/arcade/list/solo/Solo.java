package com.minecraft.savagemc.arcade.aquatic.arcade.list.solo;

import com.minecraft.savagemc.arcade.aquatic.arcade.Arcade;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.event.IgnoreEvent;

@IgnoreEvent
public class Solo extends Arcade {

    public Solo(String mapsDirectory, Integer minRooms, Integer maxRooms) {
        super(mapsDirectory, minRooms, maxRooms, ArcadeCategory.AQUATIC_SOLO);
    }
}
