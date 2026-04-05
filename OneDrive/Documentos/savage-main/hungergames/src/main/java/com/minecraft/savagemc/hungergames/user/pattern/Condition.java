/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.pattern;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Condition {

    LOADING("Carregando"), DEAD("Morto"), ALIVE("Vivo"), SPECTATOR("Espectador");

    private final String fancyName;

    @Override
    public String toString() {
        return fancyName;
    }
}
