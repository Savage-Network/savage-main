package com.minecraft.savagemc.core.bukkit.command.single;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.member.Member;
import com.minecraft.savagemc.core.member.context.elo.Elo;
import com.minecraft.savagemc.core.server.flag.ServerFlag;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class RankCommand implements CommandInheritor {

    @Command(name = "rank")
    public void rank(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cComo usar /" + context.getLabel() + ":",
                    "§c* /" + context.getLabel() + " (modo)",
                    "§c* /" + context.getLabel() + " list");
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            TextComponent message = new TextComponent("§aRanks: ");

            int index = 1;
            boolean end = false;
            for (Elo elo : Elo.values()) {
                if (index >= Elo.values().length) end = true;

                TextComponent rank = new TextComponent(elo.getColoredSymbol());
                rank.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(elo.getFullName())));

                message.addExtra(rank);
                message.addExtra("§r" + (end ? "." : ", "));

                index++;
            }

            account.send(message);
            return;
        }

        ServerType server = ServerType.of(args[0]);

        if (server == null || !server.hasFlag(ServerFlag.RANKED)) {
            account.send("§cO modo solicitado não foi encontrado, ou não possui ranks.");
            return;
        }

        Member member = account.loadMember(server);

        if (member == null) {
            account.send("§cNão foi possível carregar os dados do seu rank.");
            return;
        }

        Elo elo = member.getElo(), next = member.getNextElo();

        int current = member.getEloXp(), minXp = next.getMinXp();

        account.send("§eRank atual: " + elo.getFullName());

        if (!elo.isHighest())
            account.send(elo.getFullName() + " §f" + Constant.ARROW_SYMBOL + " " + next.getFullName()
                    + " §7(" + Util.formatNumberWithLetter(current) + "/" + Util.formatNumberWithLetter(minXp) + ") " +
                    "§e" + Util.percentage(current, minXp) + "%");
    }
}
