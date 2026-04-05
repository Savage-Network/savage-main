package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;

public class ForcewinCommand implements BukkitInterface, Assistance {

    @Command(name = "forcewin", platform = Platform.PLAYER, rank = Rank.ADMINISTRATOR, usage = "{label} <winner...>")
    public void forcewinCommand(Context<Player> context, String rawUsers) {

        Collection<User> winners = getUsers(",", rawUsers);

        if (winners.isEmpty()) {
            context.sendMessage("§cNenhum jogador encontrado.");
            return;
        }

        getGame().win(new HashSet<>(winners));
    }
}
