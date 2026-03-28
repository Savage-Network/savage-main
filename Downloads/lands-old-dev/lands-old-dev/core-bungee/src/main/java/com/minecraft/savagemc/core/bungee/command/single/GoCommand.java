package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.route.RouteContext;
import com.minecraft.savagemc.core.api.redirect.Redirect;
import com.minecraft.savagemc.core.arcade.route.join.Join;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;
import com.minecraft.savagemc.core.server.type.ServerType;

import java.util.List;

public class GoCommand implements CommandInheritor {

    @Completer(name = "go", rank = RankType.TRIAL)
    public List<String> goCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "go", rank = RankType.TRIAL)
    public void go(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline() || target.inServer(ServerType.AUTH)) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (target.inServer(account.getServerType())) {
            account.send("§cVocê já está no servidor de " + target.getNickname() + ".");
            return;
        }

        /* Redirecionar jogador */
        RouteContext route = RouteContext.copy(account.getId(), target.getRoute());

        if (route.isValidArcade())
            route.getArcade().setJoin(Join.VANISH);

        Savage.getRedirectData().save(new Redirect(account.getId(), target.getId(), route));

        account.redirect(route);
    }
}
