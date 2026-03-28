package com.minecraft.savagemc.arcade.bedwars.command;

import com.minecraft.savagemc.arcade.bedwars.arcade.Arcade;
import com.minecraft.savagemc.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.bedwars.user.User;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.arcade.room.phase.RoomPhase;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.backend.database.redis.message.types.route.arcade.ArenaSearchMessage;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class PlayCommands implements CommandInheritor {

    @Command(name = "start", rank = RankType.MODPLUS)
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

    @Command(name = "playagain")
    public void playAgain(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arcade arcade = user.getArcade();

        Arena arena = user.getArena();

        user.reset();

        new ArenaSearchMessage(player.getUniqueId(), ArcadeRouteContext.builder()
                .arcade(arcade.getCategory())
                .slot(arena.getSlot())
                .type(arena.getType())
                .join(user.getJoin())
                .build()).send();
    }
}
