/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.object;

import com.minecraft.savagemc.hungergames.user.User;
import lombok.Getter;

@Getter
public class CombatTag {

    private User lastHit;
    private long tagTime = 0;

    public void addTag(User attacker, int seconds) {
        this.lastHit = attacker;
        this.tagTime = System.currentTimeMillis() + (seconds * 1000L);
    }

    public boolean isTagged() {
        return System.currentTimeMillis() < tagTime;
    }

}
