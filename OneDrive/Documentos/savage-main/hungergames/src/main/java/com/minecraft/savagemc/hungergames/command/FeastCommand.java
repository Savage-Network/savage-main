/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.savagemc.hungergames.game.structure.Feast;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

public class FeastCommand implements Assistance {

    @Command(name = "feast", platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {

        Feast feast = getGame().getFeast();

        if (feast == null || !feast.isSpawned()) {
            context.info("hg.game.feast_not_spawned");
            return;
        }
        context.getSender().setCompassTarget(feast.getLocation());
        context.info("hg.game.user.compass_pointing_to", "Feast");
    }
}
