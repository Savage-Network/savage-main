package com.minecraft.savagemc.hungergames.user.celebrations.pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CelebrationRarity {

    COMMON(0, "Comum", "§a"),
    RARE(1, "Raro", "§b"),
    EPIC(2, "Épico", "§5"),
    LEGENDARY(3, "Lendário", "§4");

    private final int id;
    private final String name;
    private final String color;

    public final String getDisplayName() {
        return this.color + this.name;
    }

}
