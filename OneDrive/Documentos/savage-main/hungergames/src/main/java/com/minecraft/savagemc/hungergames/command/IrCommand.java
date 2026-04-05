package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.bukkit.util.BukkitInterface;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.user.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class IrCommand implements BukkitInterface {

    @Command(name = "ir", platform = Platform.PLAYER, rank = Rank.VIP, usage = "{label} <target>")
    public void execute(Context<Player> context, Player target) {
        User user = User.fetch(context.getUniqueId());

        if (user.isAlive()) {
            context.info("hg.game.cant_be_alive");
            return;
        }

        if (target == null || !target.canSee(target)) {
            context.info("target.not_found");
            return;
        }

        context.getSender().teleport(target.getLocation());
    }

    @Completer(name = "ir")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1)
            return getOnlineNicknames(context);
        return Collections.emptyList();
    }

}
