package com.minecraft.savagemc.lobby.command;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.lobby.Lobby;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class ShopBlocksCommand implements CommandInheritor {

    @Command(name = "shopblocks", aliases = {"blocos"}, rank = RankType.ADMIN)
    public void shopBlocks(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Lobby.getLoader().getArchitect().handleShopRotatingBlocks(player.getLocation());
        
        player.sendMessage("§aBlocos girando spawnados ao redor da sua localização!");
    }

    @Command(name = "removeshopblocks", aliases = {"removeblocos"}, rank = RankType.ADMIN)
    public void removeShopBlocks(BukkitCommandContext context) {
        Player player = context.getPlayer();

        int removed = 0;
        
        for (ArmorStand stand : player.getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (stand.getCustomName() != null && 
                (stand.getCustomName().equals("shop_diamond") || 
                 stand.getCustomName().equals("shop_emerald"))) {
                stand.remove();
                removed++;
            }
        }
        
        player.sendMessage("§a" + removed + " bloco(s) girando removido(s)!");
    }
}
