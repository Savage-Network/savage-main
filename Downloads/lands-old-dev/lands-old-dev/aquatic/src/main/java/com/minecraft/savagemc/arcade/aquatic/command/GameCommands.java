package com.minecraft.savagemc.arcade.aquatic.command;

import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.arcade.item.ItemList;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.util.list.bukkit.BukkitUtil;
import org.bukkit.entity.Player;

public class GameCommands implements CommandInheritor {

    @Command(name = "start", rank = RankType.MOD)
    public void start(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        if (arena.isPhase(RoomPhase.PLAYING)) {
            player.sendMessage("§cA partida já iniciou!");
            return;
        }

        arena.setPhase(RoomPhase.PLAYING);
    }

    @Command(name = "test")
    public void test(BukkitCommandContext context) {
        Player player = context.getPlayer();

        Item item = ItemList.getRandomItem();

        if (BukkitUtil.isInventoryFull(player))
            player.sendMessage("§cO seu inventário está cheio! Não há como receber itens extras.");
        else {
            player.getInventory().addItem(item);

            player.sendMessage("§aItem concedido!");
        }
    }
}
