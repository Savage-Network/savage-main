package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.friend.Friend;
import com.minecraft.savagemc.core.account.context.objects.toggle.Toggle;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.feature.ArcadeFeature;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountFriendMessage;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;
import com.minecraft.savagemc.core.server.type.ServerType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendCommand implements CommandInheritor {

    private final String NOT_FRIEND_MESSAGE = "§cVocê não possui amizade com esse jogador.";

    @Completer(name = "amigo", subCommands = {"friend"})
    public List<String> friendCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands(),
                playerList = context.getServerPlayerList();

        String[] args = context.getArgs();

        if (args.length == 1) {
            String command = args[0].toLowerCase();

            if (command.isEmpty())
                list.addAll(subCommands);
            else {
                for (String subCommand : subCommands) {
                    if (subCommand.toLowerCase().startsWith(command))
                        list.add(subCommand);
                }
            }

        } else if (args.length == 2) {
            String playerName = args[1].toLowerCase();

            if (playerName.isEmpty())
                list.addAll(playerList);
            else {
                for (String name : playerList) {
                    if (name.toLowerCase().startsWith(playerName))
                        list.add(name);
                }
            }
        }

        return list;
    }

    @Command(name = "amigo", aliases = {"friend"})
    public void friend(BungeeCommandContext context) {
        String label = "/" + context.getLabel();

        if (context.getArgs().length == 0)
            context.getSender().send("§cComo usar " + label + ":",
                    "§c* " + label + " lista (Ou /amigos)",
                    "§c* " + label + " ver (jogador)",
                    "§c* " + label + " add (jogador)",
                    "§c* " + label + " remove (jogador)",
                    "§c* " + label + " pedidos");
    }

    @Command(name = "amigo.lista", aliases = {"amigos", "friend.lista"})
    public void friendList(BungeeCommandContext context) {
        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.LIST, context.getSender().getId(), null).send();
    }

    @Command(name = "amigo.pedidos", aliases = {"friend.pedidos"})
    public void friendRequest(BungeeCommandContext context) {
        Account account = context.getAccount();

        Toggle toggle = account.getToggle();

        toggle.setAllowFriendRequests(!toggle.isAllowFriendRequests());
        account.setToggle(toggle);

        account.send(toggle.isAllowFriendRequests()
                ? "§aVocê liberou os pedidos de amizade."
                : "§cVocê desativou os pedidos de amizade.");
    }

    @Command(name = "amigo.ver", aliases = {"friend.ver"})
    public void friendSee(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " (jogador).");
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

        if (!account.isFriend(target)) {
            account.send(NOT_FRIEND_MESSAGE);
            return;
        }

        Friend friend = account.getFriend(target.getId());

        // Abrir menu de informações
        new AccountFriendMessage(AccountFriendMessage.FriendMenuType.INFO, account.getId(), friend).send();
    }

    @Command(name = "amigo.add", aliases = {"friend.add"})
    public void friendAdd(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " (jogador).");
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

        if (account.isFriend(target)) {
            account.send("§cEsse jogador já é seu amigo.");
            return;
        }

        if (!target.getToggle().isAllowFriendRequests()) {
            account.send("§cO jogador " + target.getName() + " não está recebendo pedidos de amizade!");
            return;
        }

        if (account.hasFriendRequest(target)) {
            account.send("§cVocê já enviou uma solicitação de amizade para " + target.getName() + ".");
            return;
        }

        account.send("§aVocê enviou uma solicitação de amizade para " + target.getName() + ".");
        target.addFriendRequest(account);

        TextComponent preview = new TextComponent("\n§b" + account.getName() + "§e enviou uma solicitação de amizade.");

        TextComponent acceptButton = new TextComponent("§a§lACEITAR");

        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para aceitar a solicitação.")));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amigo aceitar " + account.getName()));

        TextComponent refuseButton = new TextComponent("§c§lRECUSAR");

        refuseButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para recusar a solicitação.")));
        refuseButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/amigo recusar " + account.getName()));

        preview.addExtra("\n");
        preview.addExtra(acceptButton);
        preview.addExtra(" ");
        preview.addExtra(refuseButton);
        preview.addExtra("\n");

        target.send(preview);
    }

    @Command(name = "amigo.remove", aliases = {"friend.remove"})
    public void friendRemove(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " (jogador).");
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

        if (!account.isFriend(target)) {
            account.send(NOT_FRIEND_MESSAGE);
            return;
        }

        account.removeFriend(target);
        target.removeFriend(account);

        account.send("§4§lAMIGOS §cVocê desfez a amizade com " + target.getName() + ".");
        target.send("§eO jogador §7" + account.getName() + "§e desfez a amizade.");
    }

    @Command(name = "amigo.aceitar", aliases = {"friend.aceitar"})
    public void friendAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cNão há solicitação de amizade pendente.");
            return;
        }

        account.addFriend(target);
        target.addFriend(account);

        account.send("§aVocê aceitou a solicitação de amizade de " + target.getName() + ".");
        target.send("§a" + account.getName() + " aceitou a sua solicitação de amizade.");

        account.removeFriendRequest(target.getId());
    }

    @Command(name = "amigo.recusar", aliases = {"friend.recusar"})
    public void friendRefuse(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUtilize /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (!account.hasFriendRequest(target)) {
            account.send("§cNão há solicitação de amizade pendente.");
            return;
        }

        account.send("§cVocê recusou a solicitação de amizade de " + target.getName() + ".");
        target.send("§c" + account.getName() + " recusou a sua solicitação de amizade.");

        account.removeFriendRequest(target.getId());
    }
}
