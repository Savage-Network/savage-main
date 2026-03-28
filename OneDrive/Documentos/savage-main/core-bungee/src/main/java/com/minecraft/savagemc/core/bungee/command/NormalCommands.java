package com.minecraft.savagemc.core.bungee.command;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NormalCommands implements CommandInheritor {

    @Command(name = "ping", aliases = {"ms"})
    public void ping(BungeeCommandContext context) {
        ProxiedPlayer player = context.getPlayer();

        String[] args = context.getArgs();

        if (args.length == 0) {
            player.sendMessage(TextComponent.fromLegacyText("§eO seu ping é de §7" + Util.formatNumber(player.getPing()) + "ms§e."));
            return;
        }

        ProxiedPlayer target = context.getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(TextComponent.fromLegacyText(TARGET_NOT_FOUND));
            return;
        }

        // O ping de CassioMartim é de 20ms.
        player.sendMessage(TextComponent.fromLegacyText("§eO ping de §b" + target.getName() + "§e é de §7" + Util.formatNumber(target.getPing()) + "ms§e."));
    }

    @Command(name = "discord", aliases = {"dc"})
    public void discord(BungeeCommandContext context) {
        TextComponent message = new TextComponent("§eAcesse o nosso servidor de §9Discord§e, clicando ");

        TextComponent clickButton = new TextComponent("§a§lAQUI");

        clickButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para acessar o Discord.")));
        clickButton.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://" + Constant.SERVER_DISCORD));

        message.addExtra(clickButton);
        message.addExtra("§e.");

        context.getSender().send(message);
    }

    @Command(name = "tell", aliases = {"msg", "message", "w", "whisper"})
    public void message(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            account.send("§cUtilize /" + context.getLabel() + " (jogador) (mensagem).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (account.hasBlock(target) || target.hasBlock(account)) {
            account.send("§cVocê não pode enviar mensagem para jogadores bloqueados.");
            return;
        }

        if (!target.getToggle().isAllowMessages()) {
            account.send("§cO jogador " + target.getNickname() + " não está recebendo mensagens.");
            return;
        }

        String message = context.getMessage(1, args);

        account.setLastMessage(target.getId());
        target.setLastMessage(account.getId());

        account.send("§7Mensagem para §e" + target.getNickname() + " §8" + Constant.ARROW_SYMBOL + "§f " + message);
        target.send("§7Mensagem de §e" + account.getNickname() + " §8" + Constant.ARROW_SYMBOL + "§f " + message);
    }

    @Command(name = "reply", aliases = {"responder", "r"})
    public void reply(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (!account.hasLastMessage()) {
            account.send("§cVocê não tem mensagens pendentes.");
            return;
        }

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getLabel() + " (mensagem).");
            return;
        }

        Account target = context.getAccount(account.getContext().getLastMessage());

        account.setLastMessage(Constant.DEFAULT_ID);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.hasBlock(target) || target.hasBlock(account)) {
            account.send("§cVocê não pode enviar mensagem para jogadores bloqueados.");
            return;
        }

        String message = context.getMessage(0, args);

        account.send("§7Resposta para §e" + target.getNickname() + " §8" + Constant.ARROW_SYMBOL + "§f " + message);
        target.send("§7Resposta de §e" + account.getNickname() + " §8" + Constant.ARROW_SYMBOL + "§f " + message);
    }
}
