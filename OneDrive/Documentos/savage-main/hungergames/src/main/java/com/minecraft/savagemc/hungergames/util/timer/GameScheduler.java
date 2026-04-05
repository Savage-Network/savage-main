/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.util.timer;

import com.minecraft.core.bukkit.scheduler.GameRunnable;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.game.GameTimeEvent;
import lombok.Getter;

public class GameScheduler extends GameRunnable {

    @Getter
    private final HungerGames hungerGames;

    public GameScheduler(HungerGames hungerGames) {
        this.hungerGames = hungerGames;
    }

    @Override
    public void run() {
        new GameTimeEvent().fire();
    }

    public void schedule() {
        this.runTaskTimer(getHungerGames(), 20, 20);
    }
}
