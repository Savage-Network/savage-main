package com.minecraft.savagemc.core.bukkit.command.single;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.medal.Medal;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.bukkit.manager.list.MedalManager;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MedalCommand implements CommandInheritor {

    @Completer(name = "medal", subCommands = {"medalha"})
    public List<String> medalCompleter(BukkitCommandContext context) {
        Account account = context.getAccount();

        List<String> list = new ArrayList<>(),
                medals = MedalManager.getAvailableMedals(account).stream().map(Medal::getName).collect(Collectors.toList());

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String medalName = args[0].toLowerCase();

            for (String medal : medals) {
                if (medal.toLowerCase().startsWith(medalName))
                    list.add(medal);
            }
        } else
            list.addAll(medals);

        return list;
    }

    @Command(name = "medal", aliases = {"medalha"})
    public void medal(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            MedalManager.sendMedals(account);
            return;
        }

        Medal medal = Medal.of(args[0]);

        if (medal == null || !account.hasMedal(medal)) {
            account.send("§cA medalha solicitada não foi encontrada ou você não a possui.");
            return;
        }

        if (account.isUsingMedal(medal)) {
            account.send("§cVocê já selecionou a medalha " + medal.getColoredName() + "§c.");
            return;
        }

        account.setMedal(medal);

        account.send("§eA medalha " + medal.getColoredName() + "§e foi selecionada.");
    }
}
