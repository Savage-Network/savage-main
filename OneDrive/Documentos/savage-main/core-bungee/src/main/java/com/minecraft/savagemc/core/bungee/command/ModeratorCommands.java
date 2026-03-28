package com.minecraft.savagemc.core.bungee.command;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;

import java.util.List;
import java.util.stream.Collectors;

public class ModeratorCommands implements CommandInheritor {

    @Command(name = "staffchat", aliases = {"sc"}, rank = RankType.TRIAL)
    public void staffChat(BungeeCommandContext context) {
        Account account = context.getAccount();

        Toggle toggle = account.getToggle();

        toggle.setStaffChat(!toggle.isStaffChat());
        account.setToggle(toggle);

        account.send(toggle.isStaffChat() ? "§aVocê entrou no staffchat." : "§cVocê saiu do staffchat.");
    }

    @Command(name = "broadcast", aliases = {"bc"}, rank = RankType.ADMIN, onlyPlayer = false)
    public void broadcast(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUtilize /" + context.getLabel() + " (mensagem).");
            return;
        }

        String message = context.getMessage(0, args);

        Savage.getPlatform().sendBroadcast(message);
    }

    @Command(name = "playerfinder", aliases = {"pf"}, rank = RankType.TRIAL, runAsync = true, onlyPlayer = false)
    public void playerfinder(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            sender.send("§cUtilize /" + context.getLabel() + " (jogador).");
            return;
        }

        Account account = context.getAccount(args[0]);

        if (account == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        List<Account> list = Savage.getAccountData().getAlts(account.getIpAddress())
                .stream()
                .filter(alt -> !alt.equals(account))
                .collect(Collectors.toList());

        if (list.isEmpty())
            sender.send("§cEste jogador não possui contas alternativas.");
        else {
            StringBuilder builder = new StringBuilder();

            builder.append("§aContas Alternativas (§f").append(list.size()).append("§a): ");

            int index = 1;
            boolean end = false;
            for (Account alt : list) {
                if (index >= list.size()) end = true;

                builder.append("§f").append(account.getName()).append(end ? "." : ", ");

                index++;
            }

            sender.send(builder.toString());
        }
    }
}
