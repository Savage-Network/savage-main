/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Nenhum extends Kit {

    public Nenhum(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.ITEM_FRAME));
    }

    @Override
    public void grant(Player player) {
    }

    @Override
    public void setActive(boolean bool, boolean b) {
    }

    @Override
    public void removeItems(Player player) {
    }

    @Override
    public boolean isCombatCooldown() {
        return false;
    }
}
