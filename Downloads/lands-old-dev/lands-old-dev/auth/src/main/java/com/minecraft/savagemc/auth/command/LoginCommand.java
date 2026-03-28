package com.minecraft.savagemc.auth.command;

import com.minecraft.savagemc.auth.user.User;
import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.auth.state.AuthState;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.server.type.ServerType;

public class LoginCommand implements CommandInheritor {

    @Command(name = "login", aliases = {"logar"})
    public void login(BukkitCommandContext context) {
        User user = (User) User.of(context.getSender().getId());

        Account account = user.getAccount();

        if (account.isAuthState(AuthState.PENDENT)) {
            account.send("§cUtilize §e/register (senha) (confirmar-senha)§c para se cadastrar.");
            return;
        }

        if (!user.isLocked()) {
            account.send("§cVocê já se autenticou no servidor.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getLabel() + " (senha).");
            return;
        }

        String password = args[0];

        if (!account.samePassword(password)) {
            if (user.getTotalAttempts() <= 1) {
                context.getPlayer().kickPlayer(
                        Constant.SERVER_TITLE
                                + "\n\n§cO limite de tentativas foi excedido."
                                + "\n§cAcesse o servidor, e tente novamente!"
                                + "\n\n§cEm caso de problemas, contate: §e" + Constant.SERVER_DISCORD
                );
                return;
            }

            user.setTotalAttempts(user.getTotalAttempts() - 1);
            account.send("§cVocê digitou a sua senha incorretamente! +" + user.getTotalAttempts() + " tentativa"
                    + (user.getTotalAttempts() > 1 ? "s" : "") + ".");

            return;
        }

        user.setLocked(false);

        // Redirecionar jogador para o Lobby Principal
        account.redirectToHub();
    }
}
