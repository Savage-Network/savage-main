package com.minecraft.savagemc.arcade.bedwars.arcade.arena.context;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.event.Event;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.Top;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.type.BedDestructionTop;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.top.type.FinalKillTop;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.ore.OreGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ArenaContext {

    private Event event;

    private final List<Top> tops = new ArrayList<>(Arrays.asList(new FinalKillTop(), new BedDestructionTop()));

    private final List<OreGenerator> generators = new ArrayList<>();

    private boolean counterEnabled = true;

    public Top getTop(TopType type) {
        return tops.stream().filter(top -> top.getType().equals(type)).findFirst().orElse(null);
    }
}
