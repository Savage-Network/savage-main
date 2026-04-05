package com.minecraft.savagemc.arcade.bedwars.structure.generator.objects.type.forge;

import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.structure.generator.Generator;
import com.minecraft.savagemc.arcade.bedwars.structure.team.Team;
import com.minecraft.savagemc.core.member.list.bedwars.objects.enums.BedOre;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ForgeGenerator extends Generator {

    private final Arena arena;
    private final Team team;

    private long originTime, updateAt;

    private boolean done = true;

    public ForgeGenerator(BedOre ore, Arena arena, Team team, long updateAt) {
        super(ore, arena.getLocation("generator_" + team.getTypeName().toLowerCase()));

        this.arena = arena;
        this.team = team;

        this.originTime = updateAt;
        
        // Aplicar multiplicador de velocidade da sala custom
        long baseTime = updateAt;
        if (arena.getCustom() != null) {
            int forgeSpeed = arena.getCustom().getForgeSpeed();
            if (forgeSpeed > 1) {
                baseTime = updateAt / forgeSpeed;
            }
        }
        
        this.updateAt = System.currentTimeMillis() + baseTime;
    }

    public abstract void spawn();

    @Override
    public boolean hasPendentUpdate() {
        return done && updateAt > -1L && updateAt <= System.currentTimeMillis();
    }

    public void resetTime() {
        // Aplicar multiplicador de velocidade da sala custom
        long baseTime = originTime;
        
        if (arena.getCustom() != null) {
            int forgeSpeed = arena.getCustom().getForgeSpeed();
            if (forgeSpeed > 1) {
                baseTime = originTime / forgeSpeed;
            }
        }
        
        this.updateAt = System.currentTimeMillis() + baseTime;
    }
}
