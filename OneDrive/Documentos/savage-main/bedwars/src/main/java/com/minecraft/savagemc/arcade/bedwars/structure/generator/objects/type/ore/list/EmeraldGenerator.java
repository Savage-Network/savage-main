package com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.ore.list;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.ore.OreGenerator;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;

public class EmeraldGenerator extends OreGenerator {

    public EmeraldGenerator(int id, Arena arena) {
        super(id, arena, BedOre.EMERALD, 60);

        setMaxStackSize(2);
    }

    @Override
    public void spawn() {
        drop(getArena(), getLocation());
    }
}
