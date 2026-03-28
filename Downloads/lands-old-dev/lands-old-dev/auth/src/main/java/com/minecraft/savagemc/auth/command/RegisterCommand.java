package com.minecraft.savagemc.auth.command;

import com.minecraft.savagemc.auth.user.User;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.auth.state.AuthState;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.server.type.ServerType;

public class RegisterCommand implements CommandInheritor {

    @Command(name = "register", aliases = {"registrar"})
    public void register(BukkitCommandContext context) {
        User user = (User) User.of(context.getSender().getId());

        Account account = user.getAccount();

        if (account.isAuthState(AuthState.OK)) {
            account.send("§cUtilize §e/login (senha)§c para se autenticar.");
            return;
        }

        if (!user.isLocked()) {
            account.send("§cVocê já se autenticou no servidor.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length <= 1) {
            account.send("§cUtilize /" + context.getLabel() + " (senha) (confirmar-senha).");
            return;
        }

        String password = args[0], confirmPassword = args[1];

        if (password.length() < 4) {
            account.send("§cA sua senha não pode ter menos de 4 caractéres.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            account.send("§cAs senhas informadas não coincidem, tente novamente!");
            return;
        }

        user.setLocked(false);

        // Redirecionar jogador para Lobby Principal
        account.setPassword(password);
        account.redirectToHub();
    }
}
