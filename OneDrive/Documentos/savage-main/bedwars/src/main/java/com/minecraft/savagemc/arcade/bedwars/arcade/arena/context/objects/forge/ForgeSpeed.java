package com.minecraft.savagemc.arcade.bedwars.arcade.arena.context.objects.forge;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ForgeSpeed {

    NORMAL("Padrão", 1),
    FAST_3X("3x", 3),
    FAST_4X("4x", 4),
    FAST_5X("5x", 5);

    private final String name;
    private final int multiplier;

    public ForgeSpeed next() {
        ForgeSpeed[] values = values();
        int nextIndex = (ordinal() + 1) % values.length;
        return values[nextIndex];
    }

    public static ForgeSpeed fromMultiplier(int multiplier) {
        for (ForgeSpeed speed : values()) {
            if (speed.multiplier == multiplier) {
                return speed;
            }
        }
        return NORMAL;
    }
}
