/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.bo3;

import org.bukkit.Location;

import java.util.Collections;
import java.util.Set;

public class EmptyBO3 extends BO3 {

    @Override
    public Set<BO3Block> getBlocks() {
        return Collections.emptySet();
    }

    @Override
    public void spawn(Location location, BlockHandle blockHandle) {
    }
}
