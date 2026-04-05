package com.minecraft.savagemc.hungergames.command;

import com.minecraft.savagemc.core.bukkit.command.structure.annotation.BukkitCommand;
import com.minecraft.savagemc.core.bukkit.command.structure.annotation.CommandTarget;
import com.minecraft.savagemc.core.bukkit.command.structure.annotation.CommandType;
import com.minecraft.savagemc.core.bukkit.command.structure.annotation.CommandUsage;
import com.minecraft.savagemc.core.bukkit.user.User;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

@BukkitCommand(
        name = "start",
        description = "Inicia o jogo",
        target = CommandTarget.PLAYER,
        usage = "/start",
        type = CommandType.MODERATOR
)
public class StartCommand implements Assistance {

    @CommandUsage
    public void startCommand(User user, Player player, String[] args) {
        if (hasStarted()) {
            player.sendMessage("§cO jogo já foi iniciado.");
        } else {
            getGame().start();
            player.sendMessage("§aJogo iniciado com sucesso!");
        }
    }
}
