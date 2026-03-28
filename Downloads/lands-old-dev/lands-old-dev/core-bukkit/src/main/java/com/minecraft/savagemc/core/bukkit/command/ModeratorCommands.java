package com.minecraft.savagemc.core.bukkit.command;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import com.minecraft.savagemc.core.bukkit.api.option.ServerOptions;
import com.minecraft.savagemc.core.bukkit.api.vanish.Vanish;
import com.minecraft.savagemc.core.bukkit.command.structure.BukkitCommandContext;
import com.minecraft.savagemc.core.bukkit.menu.server.report.ReportMenu;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class ModeratorCommands implements CommandInheritor {

    @Command(name = "reports", rank = RankType.TRIAL)
    public void reports(BukkitCommandContext context) {
        new ReportMenu(context.getPlayer()).handle();
    }

    @Command(name = "gamemode", aliases = {"gm"}, rank = RankType.STUDIO)
    public void gameMode(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso /" + context.getLabel() + " (modo).");
            return;
        }

        GameMode mode = GameMode.getByObject(args[0]);

        if (mode == null) {
            player.sendMessage("§cO modo de jogo solicitado não foi encontrado.");
            return;
        }

        String modeName = mode.getAliases().get(0);

        if (args.length == 2) {
            Player target = context.getPlayer(args[1]);

            if (target == null) {
                player.sendMessage(TARGET_NOT_FOUND);
                return;
            }

            if (target.equals(player)) {
                player.sendMessage(SAME_PLAYER);
                return;
            }

            if (target.inGameMode(mode)) {
                player.sendMessage("§cO jogador " + target.getName() + " já está no " + modeName + ".");
                return;
            }

            target.setGameMode(mode);
            player.sendMessage("§eVocê alterou o modo de jogo de §6" + target.getName() + "§e para §6" + modeName + "§e.");

            log(context.getSender(), player.getName() + " atualizou o modo de jogo de " + target.getName() + " para " + modeName);
            return;
        }

        if (player.inGameMode(mode)) {
            player.sendMessage("§cO modo de jogo solicitado já foi selecionado.");
            return;
        }

        player.setGameMode(mode);
        player.sendMessage("§eVocê entrou no modo §6" + modeName + "§e.");

        log(context.getSender(), player.getName() + " entrou no modo " + modeName);
    }

    @Command(name = "vanish", aliases = {"admin", "v"}, rank = RankType.STUDIO)
    public void vanish(BukkitCommandContext context) {
        Vanish.handle(context.getPlayer());
    }

    @Command(name = "chat", rank = RankType.TRIAL, onlyPlayer = false)
    public void chat(BukkitCommandContext context) {
        String label = "/" + context.getLabel();

        context.getSender().send("§cComo usar " + label + ":",
                "§c* " + label + " (clear) (Ou /cc)",
                "§c* " + label + " (stats)");
    }

    @Command(name = "chat.clear", aliases = {"cc"}, rank = RankType.TRIAL, onlyPlayer = false)
    public void chatClear(BukkitCommandContext context) {
        for (int i = 0; i < 100; i++)
            Savage.getAccountController().send("");

        Savage.getAccountController().send("§aO chat foi limpo.");
    }

    @Command(name = "chat.stats", rank = RankType.TRIAL, onlyPlayer = false)
    public void chatStats(BukkitCommandContext context) {
        ServerOptions.CHAT_ENABLED = !ServerOptions.CHAT_ENABLED;

        Savage.getAccountController().send(ServerOptions.CHAT_ENABLED ? "§aO chat foi ativado." : "§cO chat foi desativado.");
    }

    @Command(name = "build", rank = RankType.MODPLUS)
    public void build(BukkitCommandContext context) {
        Account account = context.getAccount();

        Toggle toggle = account.getToggle();

        toggle.setAllowBuild(!toggle.isAllowBuild());
        account.setToggle(toggle);

        account.send(toggle.isAllowBuild() ? "§aO seu modo de construção foi ativado." : "§cO seu modo de construção foi desativado.");
    }

    @Command(name = "teleport", aliases = {"tp"}, rank = RankType.STUDIO)
    public void teleport(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUtilize /" + context.getLabel() + " (jogador).");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        player.teleport(target);
    }

    @Command(name = "invsee", aliases = {"inventorysee", "inv"}, rank = RankType.STUDIO)
    public void inventorySee(BukkitCommandContext context) {
        Player player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage("§cUso: /" + context.getLabel() + " (jogador).");
            return;
        }

        Player target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TARGET_NOT_FOUND);
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(SAME_PLAYER);
            return;
        }

        player.openInventory(target.getInventory());

        log(context.getSender(), player.getName() + " abriu o inventário de " + target.getName());
    }
}
