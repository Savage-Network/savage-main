package com.minecraft.savagemc.hungergames.util.bo3;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class BO3Block {

    @Getter
    private final int x, y, z;
    @Getter
    private final Pattern pattern;
}
