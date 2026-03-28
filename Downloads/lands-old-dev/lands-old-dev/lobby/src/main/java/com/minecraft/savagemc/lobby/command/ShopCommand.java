package com.minecraft.savagemc.lobby.command;

import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.lobby.menu.shop.ShopMenu;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandInheritor {

    @Command(name = "loja", aliases = {"shop", "store", "pix"})
    public void shop(BukkitCommandContext context) {
        Player player = context.getPlayer();

        new ShopMenu(player).handle();
    }
}
