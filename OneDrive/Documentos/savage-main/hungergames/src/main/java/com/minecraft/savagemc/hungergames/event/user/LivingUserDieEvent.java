/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.event.user;

import com.minecraft.core.bukkit.event.handler.ServerEvent;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.pattern.DieCause;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@AllArgsConstructor
public class LivingUserDieEvent extends ServerEvent {

    private final User user, killer;
    private final boolean countStats;
    private final DieCause dieCause;
    private final List<ItemStack> drops;
    private Location locationToDrop;

    @Override
    public void fire() {
        if (HungerGames.getInstance().getGame().getStage() == GameStage.VICTORY)
            return;
        super.fire();
    }

    public void setLocationToDrop(Location locationToDrop) {
        this.locationToDrop = locationToDrop;
    }

    public boolean hasKiller() {
        return killer != null;
    }
}
