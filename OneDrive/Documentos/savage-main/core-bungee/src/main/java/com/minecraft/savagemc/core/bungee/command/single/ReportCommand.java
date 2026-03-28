package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.report.Report;
import com.minecraft.savagemc.core.api.report.context.ReportContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReportCommand implements CommandInheritor {

    @Completer(name = "report", subCommands = {"reportar", "rp", "denunciar"})
    public List<String> reportCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "report", aliases = {"reportar", "rp", "denunciar"})
    public void report(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasCooldown(Constant.REPORT_SEND_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.REPORT_SEND_COOLDOWN_KEY) + " para denunciar novamente.");
            return;
        }

        if (args.length <= 1) {
            account.send("§cUso /" + context.getLabel() + " (jogador) (motivo).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

//        if (account.equals(target)) {
//            account.send(SAME_PLAYER);
//            return;
//        }

        String reason = context.getMessage(1, args);

        Report report = Savage.getReportData().of(target.getId());

        if (report.hasContext(account.getId())) {
            account.send("§cAguarde para denúnciar este jogador novamente.");
            return;
        }

        report.addContext(ReportContext.builder()
                .sender(account.getId())
                .reason(reason)
                .expiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))
                .build());

        account.send("§aVocê denunciou " + target.getNickname() + " por " + reason + ".");

        if (!account.isStaffer())
            account.setCooldown(Constant.REPORT_SEND_COOLDOWN_KEY, TimeUnit.SECONDS.toMillis(10));
    }
}
