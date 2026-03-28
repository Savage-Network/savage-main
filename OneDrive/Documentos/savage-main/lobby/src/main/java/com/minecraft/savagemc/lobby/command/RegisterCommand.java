package com.minecraft.savagemc.lobby.command;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.lobby.Lobby;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandInheritor {

    @Command(name = "register", aliases = {"caixa", "registradora"}, rank = RankType.ADMIN)
    public void register(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Lobby.getLoader().getArchitect().handleShopCashRegisterStand(player.getLocation());
        
        player.sendMessage("§aCaixa registradora spawnada na sua localização!");
    }

    @Command(name = "removeregister", aliases = {"removecaixa"}, rank = RankType.ADMIN)
    public void removeRegister(BukkitCommandContext context) {
        Player player = context.getPlayer();

        int removed = 0;
        
        for (ArmorStand stand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (stand.getHelmet() != null && stand.getHelmet().hasItemMeta()) {
                stand.remove();
                removed++;
            }
        }
        
        player.sendMessage("§a" + removed + " caixa(s) registradora(s) removida(s)!");
    }
}
