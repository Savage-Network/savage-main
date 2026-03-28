package com.minecraft.savagemc.arcade.pvp.command;

import com.minecraft.savagemc.arcade.pvp.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.pvp.user.User;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandInheritor {

    @Command(name = "spawn")
    public void spawn(BukkitCommandContext context) {
        Player player = context.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        if (user.isProtected() && player.getLocation().distance(arena.getLocation("spawn")) < 15) {
            player.sendMessage("§cVocê já está no spawn.");
            return;
        }

        arena.spawn(player);
    }
}
