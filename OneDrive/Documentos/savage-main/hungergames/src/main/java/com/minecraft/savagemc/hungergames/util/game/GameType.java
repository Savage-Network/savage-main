/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum GameType {

    DOUBLEKIT(2, "Double Kit"),
    SINGLEKIT(1, "Single Kit"),
    TRIPLEKIT(3, "Triple Kit"),
    QUADRUPLEKIT(4, "Quadruple Kit"),
    DOZE(12, "DOZE");

    private final int maxKits;
    private final String name;

    public static GameType fromString(String string) {
        return Arrays.stream(values()).filter(prefixType -> prefixType.name().equalsIgnoreCase(string)).findFirst().orElse(null);
    }
}
