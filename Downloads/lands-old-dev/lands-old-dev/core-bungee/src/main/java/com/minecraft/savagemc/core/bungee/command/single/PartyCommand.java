package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.party.Party;
import com.minecraft.savagemc.core.api.party.request.PartyRequest;
import com.minecraft.savagemc.core.api.party.type.PartyType;
import com.minecraft.savagemc.core.arcade.route.ArcadeRouteContext;
import com.minecraft.savagemc.core.backend.data.list.api.PartyData;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountPartyListMessage;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PartyCommand implements CommandInheritor {

    private final PartyData data = Savage.getPartyData();

    /* Messages */
    private final String IN_PARTY_MESSAGE = "§cVocê já está em uma party!",
            NOT_IN_PARTY_MESSAGE = "§cVocê não está em uma party!",
            DONT_OWN_PARTY_MESSAGE = "§cVocê não é o dono da party.";

    @Completer(name = "party")
    public List<String> partyCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands(),
                playerList = context.getServerPlayerList();

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String search = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(search))
                    list.add(subCommand);
            }

            for (String playerName : playerList) {
                if (playerName.toLowerCase().startsWith(search))
                    list.add(playerName);
            }
        } else {
            list.addAll(subCommands);
            list.addAll(playerList);
        }

        return list;
    }

    @Command(name = "party")
    public void party(BungeeCommandContext context) {
        Account account = context.getAccount();

        List<String> subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        String label = "/" + context.getLabel();

        if (args.length == 0) {
            account.send("§cComo usar " + label + ":",
                    "§c* " + label + " abrir (tipo) (slots) - §eAbra uma party.",
                    "§c* " + label + " acabar - §eAcabe a sua party atual.",
                    "§c* " + label + " sair - §eSaia da sua party.",
                    "§c* " + label + " (jogador) - §eConvide um jogador para a party.",
                    "§c* " + label + " list - §eVer membros de sua party.",
                    "§c* " + label + " aceitar (jogador) - §eAceitar um convite de party.",
                    "§c* " + label + " entrar (jogador) - §eEntrar em uma party pública.",
                    "§c* " + label + " transferir (jogador) - §eTransfira a party para outro membro.",
                    "§c* " + label + " chat (Ou /pc) (texto) - §eEnvie um texto no bate-papo da party.",
                    "§c* " + label + " expulsar (jogador) - §eExpulse um jogador da party.",
                    "§c* " + label + " warp - §ePuxe os membros até você.",
                    "§c* " + label + " warn - §eNotifique a sua party."
            );
            return;
        }

        if (!subCommands.contains(args[0].toLowerCase())) {
            Account target = context.getAccount(args[0]);

            if (target == null || !target.isOnline() || target.inServer(ServerType.AUTH)) {
                account.send(TARGET_NOT_FOUND);
                return;
            }

            if (account.equals(target)) {
                account.send(SAME_PLAYER);
                return;
            }

            if (target.hasParty()) {
                account.send("§cO jogador " + target.getNickname() + " já está em uma party!");
                return;
            }

            if (!target.getToggle().isAllowPartyInvite()) {
                account.send("§cO jogador " + target.getNickname() + " não está recebendo convites de party.");
                return;
            }

            if (data.hasRequest(account.getId(), target.getId())) {
                account.send("§cVocê já enviou um convite de party para " + target.getNickname() + ".");
                return;
            }

            Party party = account.hasParty() ? account.getParty() : new Party(account, PartyType.PUBLIC, 12);

            if (!party.isAuthor(account.getId())) {
                account.send(DONT_OWN_PARTY_MESSAGE);
                return;
            }

            TextComponent message = new TextComponent("\n");

            TextComponent acceptButton = new TextComponent("§a§lCLIQUE AQUI");
            acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party aceitar " + account.getName()));

            message.addExtra(account.getColoredName() + "§e convidou você para uma party");
            message.addExtra("\n");

            message.addExtra(acceptButton);
            message.addExtra("§e para aceitar o convite.");
            message.addExtra("\n");

            target.send(message);

            party.send(target.getColoredName() + "§e foi convidado para a party.");

            data.request(new PartyRequest(party.getIdentifier(), account.getId(), target.getId()));
        }
    }

    @Command(name = "party.criar", aliases = {"party.abrir"})
    public void partyCreate(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account.hasParty()) {
            account.send(IN_PARTY_MESSAGE);
            return;
        }

        String[] args = context.getArgs();

        /* Definindo tipo da Party */
        PartyType type = PartyType.PUBLIC;
        if (args.length == 1)
            type = PartyType.of(args[0]);

        /* Definindo capacidade de slots */
        int slots = 12;
        if (args.length == 2 && Util.isNumber(args[1]))
            slots = Integer.parseInt(args[1]);

        if (slots > 25)
            slots = 12;

        /* Criando Party */
        new Party(account, type, slots);

        account.send("§aA sua party " + type.getName().toLowerCase() + " foi criada.");
    }

    @Command(name = "party.apagar", aliases = {"party.acabar"})
    public void partyDelete(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasParty()) {
            account.send(NOT_IN_PARTY_MESSAGE);
            return;
        }

        Party party = account.getParty();

        if (!party.isAuthor(account.getId())) {
            account.send(DONT_OWN_PARTY_MESSAGE);
            return;
        }

        party.disband();
    }

    @Command(name = "party.sair")
    public void partyLeave(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasParty()) {
            account.send(NOT_IN_PARTY_MESSAGE);
            return;
        }

        Party party = account.getParty();

        if (party.isAuthor(account.getId())) {
            account.send("§cVocê não pode sair da sua party.");
            return;
        }

        party.remove(account);
    }

    @Command(name = "party.list")
    public void partySee(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasParty()) {
            account.send(handlePartyInfo(account.getParty()));
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null || !target.isOnline()) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (!target.hasParty()) {
            account.send("§cO jogador " + target.getNickname() + " não está em uma party!");
            return;
        }

        account.send(handlePartyInfo(target.getParty()));
    }

    protected String handlePartyInfo(Party party) {
        StringBuilder builder = new StringBuilder();

        builder.append("§aMembros (§f").append(party.getMembersCount()).append("/").append(party.getSlots()).append("§a): ");

        List<Account> members = party.getOnlineMembers();

        int index = 1;
        boolean end = false;
        for (Account member : members) {
            if (index >= members.size()) end = true;

            builder.append("§f").append(member.getNickname()).append(end ? "." : ", ");
            index++;
        }

        return builder.toString();
    }

    @Command(name = "party.aceitar")
    public void partyAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasParty()) {
            account.send(IN_PARTY_MESSAGE);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
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

        if (!target.hasParty()) {
            account.send("§cO jogador " + target.getNickname() + " não está em uma party!");
            return;
        }

        if (!data.hasRequest(target.getId(), account.getId())) {
            account.send("§cVocê não recebeu convites de party do jogador " + target.getNickname() + ".");
            return;
        }

        PartyRequest request = data.getRequest(target.getId(), account.getId());

        Party party = data.of(request.getPartyIdentifier());

        data.cancelRequest(request);

        if (party == null)
            account.send("§cNão foi possível encontrar a party desejada.");
        else {

            account.send("§aVocê aceitou o convite de party do jogador " + target.getNickname());
            target.send("§a" + account.getNickname() + " aceitou o seu convite de party.");

            party.add(account);
        }
    }

    @Command(name = "party.transferir")
    public void partyTransfer(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (!account.hasParty()) {
            account.send(NOT_IN_PARTY_MESSAGE);
            return;
        }

        Party party = account.getParty();

        if (!party.isAuthor(account.getId())) {
            account.send(DONT_OWN_PARTY_MESSAGE);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
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

        if (!party.isMember(target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " não faz parte da sua party!");
            return;
        }

        party.transfer(target);
    }

    @Command(name = "party.chat", aliases = {"pc"})
    public void partyChat(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasParty()) {
            account.send(NOT_IN_PARTY_MESSAGE);
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (texto).");
            return;
        }

        Party party = account.getParty();

        // Enviar mensagem de acordo com o prefix type do jogador
        for (Account member : party.getOnlineMembers())
            member.send(Constant.PARTY_CHAT_PREFIX + account.getTag().getByPrefix(member.getTagPrefix()) + account.getNickname() + ": §f" + context.getMessage(0, args));
    }

    @Command(name = "party.expulsar")
    public void partyKick(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasParty()) {
            account.setParty(NOT_IN_PARTY_MESSAGE);
            return;
        }

        Party party = account.getParty();

        if (!party.isAuthor(account.getId())) {
            account.setParty(DONT_OWN_PARTY_MESSAGE);
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
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

        if (!party.isMember(target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " não é um membro da sua party!");
            return;
        }

        party.kick(target);
    }

    @Command(name = "party.entrar")
    public void partyJoin(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (account.hasParty()) {
            account.send(IN_PARTY_MESSAGE);
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            new AccountPartyListMessage(account.getId()).send();
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

        if (!target.hasParty()) {
            account.send("§cO jogador " + target.getNickname() + " não está em uma party!");
            return;
        }

        Party party = target.getParty();

        if (!party.isType(PartyType.PUBLIC)) {
            account.send("§cA party do jogador " + target.getNickname() + " não está aberta ao público.");
            return;
        }

        if (party.isFull()) {
            account.send("§cA party do jogador " + target.getNickname() + " está cheia.");
            return;
        }

        party.add(account);

        account.send("§aVocê entrou na party de " + target.getNickname() + ".");
    }

    @Command(name = "party.warp", aliases = {"party.puxar"})
    public void partyWarp(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasParty()) {
            account.setParty(NOT_IN_PARTY_MESSAGE);
            return;
        }

        Party party = account.getParty();

        if (!party.isAuthor(account.getId())) {
            account.setParty(DONT_OWN_PARTY_MESSAGE);
            return;
        }

        ArcadeRouteContext route = account.getArcadeRoute();

        route.setLink(party.getMembersId());

        /* Puxar membros até o dono */
        party.redirect(route);
    }

    @Command(name = "party.warn")
    public void partyWarn(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasParty()) {
            account.send(NOT_IN_PARTY_MESSAGE);
            return;
        }

        if (!account.isPartyOwner()) {
            account.send(DONT_OWN_PARTY_MESSAGE);
            return;
        }

        if (!account.inServer(ServerType.HUB)) {
            account.send("§cVocê não pode compartilhar a sua party nesse servidor.");
            return;
        }

        if (account.hasCooldown(Constant.PARTY_WARN_COOLDOWN_KEY)) {
            account.send("§cAguarde " + account.getFormattedCooldown(Constant.PARTY_WARN_COOLDOWN_KEY) + " para compartilhar sua party novamente.");
            return;
        }

        Party party = account.getParty();

        if (!party.isType(PartyType.PUBLIC)) {
            account.send("§cA sua party precisa ser pública para ser divulgada.");
            return;
        }

        TextComponent message = new TextComponent(Constant.PARTY_CHAT_PREFIX + account.getColoredName() + "§e está com a party aberta. ");

        TextComponent joinButton = new TextComponent("§b§lCLIQUE AQUI");
        joinButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party entrar " + account.getName()));

        message.addExtra(joinButton);
        message.addExtra("§e para entrar!");

        broadcast(search -> search.inServer(ServerType.HUB), message);

        if (!account.isStaffer())
            account.setCooldown(Constant.PARTY_WARN_COOLDOWN_KEY, TimeUnit.MINUTES.toMillis(5));
    }
}
