package com.minecraft.savagemc.core.bukkit.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.reconnect.Reconnect;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;

public class ReconnectCommand implements CommandInheritor {

    @Command(name = "reconectar", aliases = {"retornar", "reconnect"})
    public void reconnect(BukkitCommandContext context) {
        Account account = context.getAccount();

        Reconnect reconnect = Savage.getReconnectData().of(account.getId());

        if (reconnect == null) {
            account.send("§cNão há jogos em andamento.");
            return;
        }

        new ArenaSearchMessage(account.getId(), reconnect.getRoute()).send();
    }
}
