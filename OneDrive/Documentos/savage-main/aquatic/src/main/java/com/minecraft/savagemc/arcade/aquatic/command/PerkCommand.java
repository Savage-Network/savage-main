package com.minecraft.savagemc.arcade.aquatic.command;

import com.minecraft.savagemc.arcade.aquatic.arcade.arena.Arena;
import com.minecraft.savagemc.arcade.aquatic.controller.PerkController;
import com.minecraft.savagemc.arcade.aquatic.menu.perk.PerkMenu;
import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.arcade.aquatic.user.User;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;

public class PerkCommand implements CommandInheritor {

    @Command(name = "perk", aliases = {"kit"})
    public void perk(BukkitCommandContext context) {
        Account account = context.getAccount();

        User user = (User) User.of(account.getId());

        Arena arena = user.getArena();

        if (!arena.isPreGame()) {
            account.send("§cVocê não pode selecionar perks neste momento.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            new PerkMenu(context.getPlayer()).handle();
            return;
        }

        Perk perk = PerkController.of(context.getMessage(0, args));

        if (perk == null) {
            account.send("§cA perk solicitada não foi encontrada.");
            return;
        }

        if (!perk.has(account)) {
            account.send("§cVocê não possui a perk " + perk.getName() + ".");
            return;
        }

        if (user.isUsingPerk(perk.getName())) {
            account.send("§cVocê já está usando a perk " + perk.getName() + "!");
            return;
        }

        user.setPerk(perk);

        account.title("§6§l" + perk.getName(), "§eSelecionada!");

        account.send("§aVocê selecionou a perk " + perk.getName() + ".");

    }
}
