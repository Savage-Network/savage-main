package com.minecraft.savagemc.core.bukkit.command.single;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.bukkit.event.type.account.AccountPrefixTypeEvent;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrefixTypeCommand implements CommandInheritor {

    @Completer(name = "prefixtype", subCommands = {"pft"}, rank = RankType.SAVAGE)
    public List<String> prefixTypeCompleter(BukkitCommandContext context) {
        List<String> list = new ArrayList<>(),
                prefixList = TagPrefix.list().stream().map(prefix -> prefix.name().toLowerCase()).collect(Collectors.toList());

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String prefixName = args[0].toLowerCase();

            for (String prefix : prefixList) {
                if (prefix.toLowerCase().startsWith(prefixName))
                    list.add(prefix);
            }
        } else
            list.addAll(prefixList);

        return list;
    }

    @Command(name = "prefixtype", aliases = {"pft"}, rank = RankType.SAVAGE)
    public void prefixType(BukkitCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (tipo).");
            return;
        }

        TagPrefix prefix = TagPrefix.of(args[0]);

        if (prefix == null) {
            account.send("§cO prefixo \"" + args[0] + "\" não foi encontrado.");
            return;
        }

        if (account.isUsingTagPrefix(prefix)) {
            account.send("§cVocê já está usando o prefixo " + prefix.getName() + ".");
            return;
        }

        account.setTagPrefix(prefix);

        account.send("§aO prefixo " + prefix.getName() + " foi selecionado.");

        new AccountPrefixTypeEvent(account, prefix).call();
    }
}
