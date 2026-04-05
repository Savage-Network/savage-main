/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.object;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AwaySession {

    private int remainingTime;
    private final boolean locked;

    public boolean expired() {
        return remainingTime <= 0;
    }

    public void invalidate() {
        this.remainingTime = Integer.MAX_VALUE;
    }

    public void decrease() {
        this.remainingTime--;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
