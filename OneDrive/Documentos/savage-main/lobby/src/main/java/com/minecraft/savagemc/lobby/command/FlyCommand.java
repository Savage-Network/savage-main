package com.minecraft.savagemc.lobby.command;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandInheritor {

    @Command(name = "fly", aliases = {"voar"}, rank = RankType.VIP)
    public void fly(BukkitCommandContext context) {
        Player player = context.getPlayer();

        boolean flying = !player.isFlying();
        player.setAllowFlight(flying);
        player.setFlying(flying);
        
        if (flying) {
            player.setFlySpeed(0.2f); // Velocidade de voo mais rápida (padrão é 0.1f, máximo é 1.0f)
        } else {
            player.setFlySpeed(0.1f); // Velocidade padrão
        }
        
        player.sendMessage(flying ? "§aVocê ativou o seu modo de voo." : "§cVocê desativou o seu modo de voo.");
    }
}


