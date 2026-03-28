package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;

import java.util.List;
import java.util.stream.Collectors;

public class StaffListCommand implements CommandInheritor {

    @Command(name = "stafflist", aliases = {"sclist"}, rank = RankType.MODPLUS, runAsync = true, onlyPlayer = false)
    public void staffList(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length == 0) {
            // Mostrar todos os staffers online no servidor
            buildStaffList(sender, Savage.getAccountController().filter(Account::isStaffer));
            return;
        }

        // Listar todos os staffers do servidor
        if (args[0].equalsIgnoreCase("all"))
            buildStaffList(sender, Savage.getAccountData().list().stream().filter(Account::isStaffer).collect(Collectors.toList()));
    }

    protected void buildStaffList(CommandSender sender, List<Account> staffers) {
        StringBuilder builder = new StringBuilder("§aMembros da equipe:");

        for (Account staffer : staffers) {
            Tag tag = Tag.of(staffer.getRankType());

            builder.append("\n").append("§8* ").append(tag.getPrefix()).append(staffer.getName());
        }

        sender.send(builder.toString());
    }
}
