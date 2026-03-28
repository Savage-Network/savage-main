package com.minecraft.savagemc.core.arcade.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArcadeRule {

    NONE("Não há embasamento"),
    BED("Baseado em camas"),
    POINT("Baseado em pontos");

    private final String title;
}
