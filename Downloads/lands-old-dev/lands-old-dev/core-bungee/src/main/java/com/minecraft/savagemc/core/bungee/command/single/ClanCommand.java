package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.clan.request.ClanRequest;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.clan.Clan;
import com.minecraft.savagemc.core.api.clan.role.ClanRole;
import com.minecraft.savagemc.core.backend.data.list.api.ClanData;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountClanChangeMessage;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ClanCommand implements CommandInheritor {

    private final ClanData data = Savage.getClanData();

    protected final Pattern NAME_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{3,12}$"),
            TAG_VALIDATOR = Pattern.compile("^[a-zA-Z0-9]{4,8}$");

    private final String ALREADY_IN_A_CLAN = "§cVocê já faz parte de um clan!",
            NOT_PART_OF_CLAN = "§cVocê não faz parte de um clan!";

    @Completer(name = "clan")
    public List<String> clanCompleter(BungeeCommandContext context) {
        List<String> list = new ArrayList<>(),
                subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String command = args[0].toLowerCase();

            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(command))
                    list.add(subCommand);
            }
        } else
            list.addAll(subCommands);

        return list;
    }

    @Command(name = "clan")
    public void clan(BungeeCommandContext context) {
        Account account = context.getAccount();

        String label = "/" + context.getLabel();

        List<String> subCommands = context.getSubCommands();

        String[] args = context.getArgs();

        if (args.length == 0 || !subCommands.contains(args[0].toLowerCase()))
            account.send("§cComo usar " + label + ":",
                    "§c* " + label + " criar (nome) (tag)",
                    "§c* " + label + " deletar",
                    "§c* " + label + " sair",
                    "§c* " + label + " ver (nome/tag)",
                    "§c* " + label + " convidar (jogador)",
                    "§c* " + label + " aceitar (jogador)",
                    "§c* " + label + " recusar (jogador)",
                    "§c* " + label + " transferir (jogador)"
            );
    }

    @Command(name = "clan.criar")
    public void clanCreate(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length <= 1) {
            account.send("§cUso: /" + context.getCommandLabel() + " (nome) (tag).");
            return;
        }

        String name = args[0], tag = args[1];

        if (!NAME_VALIDATOR.matcher(name).matches()) {
            account.send("§cO nome do seu clan não é válido!",
                    "§cSomente letras e números, com caractéres de 3 até 12!");
            return;
        }

        if (data.getByName(name) != null) {
            account.send("§cOps! Um clan com o nome " + name + " já existe.");
            return;
        }

        if (!TAG_VALIDATOR.matcher(tag).matches()) {
            if (!account.hasRank(RankType.ADMIN) || tag.length() < 2 || tag.length() > 8 || !tag.matches("[a-zA-Z0-9]+")) {
                account.send("§cA tag do seu clan não é válida!",
                        "§cSomente letras e números, com caractéres de 4 até 8!");
                return;
            }
        }

        if (data.getByTag(tag) != null) {
            account.send("§cOps! Um clan com a tag " + tag + " já existe.");
            return;
        }

        Clan clan = new Clan(account, UUID.randomUUID(), name, tag);

        account.send("§aO seu clan foi criado com sucesso.");
        account.setClan(clan.getId());

        data.save(clan);

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.deletar")
    public void clanDelete(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.hasClanRole(ClanRole.LEADER)) {
            account.send("§cVocê não pode deletar o seu clan atual.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (confirmar).");
            return;
        }

        if (args[0].equalsIgnoreCase("confirmar"))
            account.getClan().disband();
    }

    @Command(name = "clan.convidar")
    public void clanInvite(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.hasClanRole(ClanRole.RECRUITER)) {
            account.send("§cVocê não pode convidar jogadores para o clan.");
            return;
        }

        Clan clan = account.getClan();

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        if (!target.getToggle().isAllowClanInvite()) {
            account.send("§cO jogador " + target.getNickname() + " não está recebendo convites de clan.");
            return;
        }

        if (target.hasClan()) {
            account.send("§cO jogador " + target.getNickname() + " já faz parte de um clan!");
            return;
        }

        if (data.hasRequest(clan, target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " já foi convidado para o clan!");
            return;
        }

        account.send("§aVocê convidou " + target.getNickname() + " para o seu clan!");

        TextComponent message = new TextComponent("§6" + account.getNickname() + "§e convidou você para o clan §b" + clan.getName() + "§e!");

        TextComponent acceptButton = new TextComponent("§a§lACEITAR");

        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan aceitar " + account.getNickname()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para aceitar o convite.")));

        TextComponent declineButton = new TextComponent("§c§lRECUSAR");

        declineButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan recusar " + account.getNickname()));
        declineButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para recusar o convite.")));

        message.addExtra("\n        ");
        message.addExtra(acceptButton);
        message.addExtra("        ");
        message.addExtra(declineButton);

        // Adicionando requisição no Redis
        data.addRequest(new ClanRequest(clan.getId(), account.getId(), target.getId()));

        target.send(message);
    }

    @Command(name = "clan.aceitar")
    public void clanAccept(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        ClanRequest request = data.getRequest(target.getId(), account.getId());

        if (request == null) {
            account.send("§cNenhum convite foi encontrado de " + target.getNickname() + ".");
            return;
        }

        Clan clan = data.of(request.getId());

        data.removeRequest(request);

        if (clan == null) {
            account.send("§cNão foi possível encontrar o clan.");
            return;
        }

        clan.addMember(account);

        account.send("§aVocê aceitou o convite de " + target.getNickname() + " para entrar no clan §7" + clan.getName() + "§a.");
        target.send("§a" + account.getNickname() + " aceitou o seu convite para entrar no clan.");

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.sair")
    public void clanLeave(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (account.isClanLeader()) {
            account.send("§cVocê não pode sair do seu clan!");
            return;
        }

        Clan clan = account.getClan();

        clan.removeMember(account);

        new AccountClanChangeMessage(account).send();
    }

    @Command(name = "clan.transferir")
    public void clanTransfer(BungeeCommandContext context) {
        Account account = context.getAccount();

        if (!account.hasClan()) {
            account.send(NOT_PART_OF_CLAN);
            return;
        }

        if (!account.isClanLeader()) {
            account.send("§cVocê não pode transferir o clan para outros jogadores.");
            return;
        }

        String[] args = context.getArgs();

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        Clan clan = account.getClan();

        if (!clan.isMember(target.getId())) {
            account.send("§cO jogador " + target.getNickname() + " não é membro do seu clan.");
            return;
        }

        clan.setLeader(target.getId());

        clan.send("§7O clan foi transferido para " + target.getNickname() + ".");
    }

    @Command(name = "clan.recusar")
    public void clanDecline(BungeeCommandContext context) {
        Account account = context.getAccount();

        String[] args = context.getArgs();

        if (account.hasClan()) {
            account.send(ALREADY_IN_A_CLAN);
            return;
        }

        if (args.length == 0) {
            account.send("§cUso: /" + context.getCommandLabel() + " (jogador).");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            account.send(TARGET_NOT_FOUND);
            return;
        }

        if (account.equals(target)) {
            account.send(SAME_PLAYER);
            return;
        }

        ClanRequest request = data.getRequest(target.getId(), account.getId());

        if (request == null) {
            account.send("§cNenhum convite foi encontrado de " + target.getNickname() + ".");
            return;
        }

        data.removeRequest(request);

        account.send("§cVocê recusou o convite de " + target.getNickname() + " para entrar no clan.");
        target.send("§c" + account.getNickname() + " recusou o convite para entrar no clan.");
    }
}
