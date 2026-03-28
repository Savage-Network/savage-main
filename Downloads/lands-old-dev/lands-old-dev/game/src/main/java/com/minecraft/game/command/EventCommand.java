package com.minecraft.game.command;

import com.minecraft.game.arcade.arena.Arena;
import com.minecraft.game.user.User;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommand;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.event.IgnoreEvent;
import com.minecraft.savagemc.core.member.Member;
import org.bukkit.entity.Player;

@IgnoreEvent
public class EventCommand extends BukkitCommand {

    public EventCommand() {
        super("evento");
    }

    @Override
    public void onCommand(BukkitCommandContext context) {
        Player player = context.getPlayer();
        Account account = context.getAccount();

        Member member = Savage.getMemberData().of(player.getUniqueId());

        if (member == null) {
            account.send("§cCarregando dados...");
            return;
        }

        ArcadeRouteContext route = new ArcadeRouteContext(ArcadeCategory.EVENT_SUMO);

        Arena arena = (Arena) Savage.getArcadeData().findBestArena(player.getUniqueId(), route);

        if (arena == null) {
            account.send("§cNenhuma sala disponível no momento.");
            return;
        }

        User user = new User(member, arena, Join.PLAYER);

        user.handle();
    }
}
