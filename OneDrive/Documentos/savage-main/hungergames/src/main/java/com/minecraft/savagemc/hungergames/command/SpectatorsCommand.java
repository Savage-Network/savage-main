/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

public class SpectatorsCommand implements Assistance {

    @Command(name = "spectators", aliases = "specs", platform = Platform.PLAYER, rank = Rank.VIP)
    public void handleCommand(Context<Player> context) {
        final Player sender = context.getSender();
        final User user = getUser(sender.getUniqueId());

        boolean bool = !user.isSpecs();
        user.setSpecs(bool);

        if (bool)
            context.sendMessage("§aVocê ativou os espectadores.");
        else
            context.sendMessage("§cVocê desativou os espectadores.");

        refreshVisibility(sender);
    }

}
