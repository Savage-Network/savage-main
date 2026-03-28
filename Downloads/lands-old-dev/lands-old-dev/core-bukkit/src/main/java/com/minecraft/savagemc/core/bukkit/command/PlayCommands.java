package com.minecraft.savagemc.core.bukkit.command;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.server.type.ServerType;

public class PlayCommands implements CommandInheritor {

    @Command(name = "lobby", aliases = {"hub", "l"})
    public void hub(BukkitCommandContext context) {
        Account account = context.getAccount();

        if (account.inServer(ServerType.HUB)) {
            account.send("§cVocê já está conectado no Lobby Principal!");
            return;
        }

        ServerType server = account.getServerType();

        if (server.isLobby() && !server.equals(ServerType.HUB)) {
            account.redirect(ServerType.HUB);
            return;
        }

        account.redirect(server.getServerLobby());
    }
}
