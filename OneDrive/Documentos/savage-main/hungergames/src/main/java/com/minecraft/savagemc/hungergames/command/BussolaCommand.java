/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.bukkit.util.worldedit.WorldEditAPI;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BussolaCommand implements BukkitInterface {

    @Command(name = "bussola", aliases = { "compass", "bs" }, platform = Platform.PLAYER)
    public void handleCommand(Context<Player> context) {
        Player player = context.getSender();

        player.getInventory().addItem(new ItemFactory(Material.COMPASS).setDescription("§7Encontrar jogadores").setName("§aBússola").getStack());
        player.sendMessage("§eVocê pegou uma §bbússola §ecom sucesso!");
    }
}
