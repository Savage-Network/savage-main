package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.account.context.objects.clantag.ClanTag;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.assignment.Assignment;
import com.minecraft.savagemc.core.account.context.objects.medal.Medal;
import com.minecraft.savagemc.core.account.context.objects.permission.Permission;
import com.minecraft.savagemc.core.account.context.objects.rank.Rank;
import com.minecraft.savagemc.core.account.context.objects.rank.info.RankInfo;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.Tag;
import com.minecraft.savagemc.core.api.geography.GeographyBuilder;
import com.minecraft.savagemc.core.api.geography.GeographyService;
import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.backend.database.redis.message.types.account.AccountPunishmentsMessage;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AccountCommand implements CommandInheritor {

    private final List<String> subCommands = Arrays.asList(
            "rank", "ranks", "golds", "permission", "punishments",
            "tag", "medal", "medals", "bans", "mutes", "clantag"
    );

    @Completer(name = "account", subCommands = {"acc"}, rank = RankType.MODPLUS)
    public List<String> accountCompleter(BungeeCommandContext context) {
        return getPlayerNames(context);
    }

    @Command(name = "account", aliases = {"acc"}, onlyPlayer = false)
    public void account(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();
        Account staffer = context.getAccount();

        if (args.length == 0) {
            sendInfoMessage(sender, staffer);
            return;
        }

        if (sender.isPlayer() && !staffer.hasRank(RankType.MODPLUS)) {
            sender.send("§cVocê não pode ver a conta de outros jogadores.");
            return;
        }

        Account target = context.getAccount(args[0]);

        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (args.length == 1) {
            sendInfoMessage(sender, target);
            return;
        }

        String label = "/" + context.getLabel();
        String search = args[1].toLowerCase();

        if (subCommands.stream().noneMatch(s -> s.equalsIgnoreCase(search))) {
            sender.send("§cComo usar " + label + ":");
            subCommands.forEach(s -> sender.send("§c* " + label + " (jogador) " + s));
            return;
        }

        switch (search) {
            case "rank": handleRank(context, sender, staffer, target, args, label); break;
            case "ranks": handleRanks(sender, target); break;
            case "tag": handleTag(context, sender, target, args, label); break;
            case "medal": handleMedal(context, sender, target, args, label); break;
            case "medals": handleMedals(sender, target); break;
            case "bans": handlePunishmentList(sender, target, PunishmentCategory.BAN); break;
            case "mutes": handlePunishmentList(sender, target, PunishmentCategory.MUTE); break;
            case "permission": handlePermission(context, sender, target, args, label); break;
            case "punishments": handlePunishments(sender, target, args, label); break;
            case "golds": handleGolds(context, sender, target, args, label); break;
            case "clantag": handleClanTag(sender, target, args, label); break;
        }
    }

    private void handleRank(BungeeCommandContext context, CommandSender sender, Account staffer, Account target, String[] args, String label) {
        if (args.length == 2) {
            sender.send("§cComo usar " + label + " rank:",
                    "§c* " + label + " (jogador) rank set (tipo) [tempo]",
                    "§c* " + label + " (jogador) rank remove (tipo)",
                    "§c* " + label + " (jogador) rank info");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "set": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) rank set (tipo) [tempo]."); return; }
                RankType rankType = RankType.of(args[3]);
                if (rankType == null) { sender.send("§cRank não encontrado."); return; }
                if (sender.isPlayer() && (staffer.getRankType().equals(rankType) || !staffer.hasRank(rankType))) {
                    sender.send("§cVocê não pode atribuir ranks similares ou iguais ao seu."); return;
                }
                if (sender.isPlayer() && !staffer.hasRank(target.getRank().getType())) {
                    sender.send("§cVocê não pode atribuir ranks para jogadores superiores."); return;
                }
                if (target.hasAvailableRank(rankType) || target.hasOnlyRank(rankType)) {
                    sender.send("§cO jogador já possui o rank " + rankType.getName() + "."); return;
                }
                long expiresAt = args.length >= 5 ? TimeUtil.getTime(args[4]) : -1L;
                Rank rank = Rank.builder()
                        .type(rankType)
                        .assignment(sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE)
                        .author(sender.getId())
                        .expiresAt(expiresAt)
                        .build();
                target.setRank(rank);
                sender.send("§aRank " + rankType.getName() + " adicionado para " + target.getNickname() + ".");
                log(sender, sender.getName() + " adicionou o rank " + rankType.getName() + " para " + target.getNickname());
                break;
            }
            case "remove": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) rank remove (tipo)."); return; }
                RankType rankType = RankType.of(args[3]);
                if (rankType == null) { sender.send("§cRank não encontrado."); return; }
                if (rankType.equals(RankType.MEMBER)) { sender.send("§cNão é possível remover o rank MEMBER."); return; }
                if (!(target.hasOnlyRank(rankType) || target.hasAvailableRank(rankType))) {
                    sender.send("§cO jogador não possui o rank " + rankType.getName() + "."); return;
                }
                target.removeRank(rankType);
                sender.send("§eRank " + rankType.getName() + " removido de " + target.getNickname() + ".");
                break;
            }
            case "info": {
                RankInfo info = target.getContext().getRankInfo();
                Rank rank = info.getRank();
                List<Rank> rankList = info.getAvailableRanks();
                StringBuilder builder = new StringBuilder("§aRanks extras (§f").append(rankList.size()).append("§a): ");
                int idx = 1;
                for (Rank r : rankList) builder.append(r.getColoredName()).append("§f").append(idx++ >= rankList.size() ? "." : ", ");
                if (rankList.isEmpty()) builder.append("§fVázio.");
                sender.send("§aRank: " + rank.getColoredName(),
                        "§aAtribuído por: §f" + rank.getAuthorName(),
                        " §7Data: " + DateUtil.getDate(rank.getAssignedAt()),
                        " §6§o" + rank.getAssignment().getName(),
                        builder.toString());
                break;
            }
        }
    }

    private void handleRanks(CommandSender sender, Account target) {
        List<Rank> all = target.getAllRanks();
        if (all.isEmpty()) { sender.send("§cNenhum rank encontrado."); return; }
        sender.send("§aRanks de " + target.getNickname() + ":");
        all.forEach(r -> sender.send(" §7- " + r.getColoredName()
                + " §8| §7Autor: " + r.getAuthorName()
                + " §8| §7Data: " + DateUtil.getDate(r.getAssignedAt())
                + (r.isPermanent() ? "" : " §8| §7Expira: " + DateUtil.getDate(r.getExpiresAt()))));
    }

    private void handleTag(BungeeCommandContext context, CommandSender sender, Account target, String[] args, String label) {
        if (args.length == 2) {
            sender.send("§cComo usar " + label + " tag:",
                    "§c* " + label + " (jogador) tag set (tag)",
                    "§c* " + label + " (jogador) tag remove",
                    "§c* " + label + " (jogador) tag info");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "set": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) tag set (tag)."); return; }
                Tag tag = Tag.of(args[3]);
                if (tag == null || tag == Tag.MEMBER) { sender.send("§cTag não encontrada."); return; }
                target.setTag(tag);
                sender.send("§aTag " + tag.getColoredName() + " §adefinida para " + target.getNickname() + ".");
                log(sender, sender.getName() + " definiu a tag " + tag.getName() + " para " + target.getNickname());
                break;
            }
            case "remove": {
                target.setTag(Tag.MEMBER);
                sender.send("§eTag de " + target.getNickname() + " removida.");
                break;
            }
            case "info": {
                sender.send("§aTag atual de " + target.getNickname() + ": " + target.getTag().getColoredName());
                break;
            }
        }
    }

    private void handleMedal(BungeeCommandContext context, CommandSender sender, Account target, String[] args, String label) {
        if (args.length == 2) {
            sender.send("§cComo usar " + label + " medal:",
                    "§c* " + label + " (jogador) medal add (medal)",
                    "§c* " + label + " (jogador) medal remove (medal)",
                    "§c* " + label + " (jogador) medal set (medal)");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "add": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) medal add (medal)."); return; }
                Medal medal = Medal.of(args[3]);
                if (medal == null || medal == Medal.NONE) { sender.send("§cMedalha não encontrada."); return; }
                if (target.hasMedal(medal)) { sender.send("§cO jogador já possui essa medalha."); return; }
                target.addMedal(medal);
                sender.send("§aMedalha " + medal.getColoredName() + " §aadicionada para " + target.getNickname() + ".");
                log(sender, sender.getName() + " adicionou a medalha " + medal.getName() + " para " + target.getNickname());
                break;
            }
            case "remove": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) medal remove (medal)."); return; }
                Medal medal = Medal.of(args[3]);
                if (medal == null || medal == Medal.NONE) { sender.send("§cMedalha não encontrada."); return; }
                if (!target.hasMedal(medal)) { sender.send("§cO jogador não possui essa medalha."); return; }
                target.removeMedal(medal);
                sender.send("§eMedalha " + medal.getColoredName() + " §eremovida de " + target.getNickname() + ".");
                break;
            }
            case "set": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) medal set (medal)."); return; }
                Medal medal = Medal.of(args[3]);
                if (medal == null) { sender.send("§cMedalha não encontrada."); return; }
                target.setMedal(medal);
                sender.send("§aMedalha ativa de " + target.getNickname() + " definida para " + medal.getColoredName() + "§a.");
                break;
            }
        }
    }

    private void handleMedals(CommandSender sender, Account target) {
        List<Medal> medals = target.getMedals();
        if (medals.isEmpty()) { sender.send("§cNenhuma medalha encontrada."); return; }
        sender.send("§aMedalhas de " + target.getNickname() + ":");
        medals.forEach(m -> sender.send(" §7- " + m.getColoredName()));
    }

    private void handlePunishmentList(CommandSender sender, Account target, PunishmentCategory category) {
        List<Punishment> list = target.getPunishments(category);
        if (list.isEmpty()) { sender.send("§cNenhum " + category.getName().toLowerCase() + " encontrado para " + target.getNickname() + "."); return; }
        sender.send("§a" + category.getName() + "s de " + target.getNickname() + " (" + list.size() + "):");
        list.forEach(p -> {
            String status = p.isValid() ? "§c[ATIVO]" : "§7[INATIVO]";
            String expiry = p.isTemporary() ? " §8| §7Expira: " + DateUtil.getDate(p.getExpiresAt()) : " §8| §7Permanente";
            sender.send(" " + status + " §7#" + p.getId()
                    + " §8| §7Motivo: §f" + p.getReason().name()
                    + " §8| §7Autor: §f" + p.getAuthorName()
                    + " §8| §7Data: " + DateUtil.getDate(p.getCreatedAt()) + expiry);
        });
    }

    private void handlePermission(BungeeCommandContext context, CommandSender sender, Account target, String[] args, String label) {
        if (args.length == 2) {
            sender.send("§cComo usar " + label + " permission:",
                    "§c* " + label + " (jogador) permission set (chave) (tempo)",
                    "§c* " + label + " (jogador) permission remove (chave)",
                    "§c* " + label + " (jogador) permission list");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "set": {
                if (args.length <= 4) { sender.send("§cUso: " + label + " (jogador) permission set (chave) (tempo)."); return; }
                String key = args[3];
                if (target.hasPermission(key)) { sender.send("§cO jogador já tem essa permissão."); return; }
                long expiresAt = TimeUtil.getTime(args[4]);
                target.setPermission(new Permission(key, sender.isPlayer() ? Assignment.STAFF : Assignment.CONSOLE, sender.getId(), expiresAt));
                sender.send("§aPermissão '" + key + "' adicionada para " + target.getNickname() + ".");
                break;
            }
            case "remove": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) permission remove (chave)."); return; }
                String key = args[3];
                if (!target.hasPermission(key)) { sender.send("§cO jogador não tem essa permissão."); return; }
                target.removePermission(key);
                sender.send("§cPermissão '" + key + "' removida de " + target.getNickname() + ".");
                break;
            }
            case "list": {
                List<Permission> list = target.getPermissions();
                if (list.isEmpty()) { sender.send("§cO jogador não possui permissões."); return; }
                StringBuilder builder = new StringBuilder("§aPermissões: ");
                int idx = 1;
                for (Permission p : list) builder.append("§f").append(p.getKey()).append(idx++ >= list.size() ? "." : ", ");
                sender.send(builder.toString());
                break;
            }
        }
    }

    private void handlePunishments(CommandSender sender, Account target, String[] args, String label) {
        if (!sender.isPlayer()) { sender.send("§cSomente jogadores podem visualizar punições."); return; }
        if (args.length == 2) { sender.send("§cUso: " + label + " (jogador) punishments (categoria)."); return; }
        PunishmentCategory category = PunishmentCategory.of(args[2]);
        if (category == null) { sender.send("§cCategoria '" + args[2] + "' não encontrada."); return; }
        if (target.getPunishments(category).isEmpty()) {
            sender.send("§cO jogador não possui histórico de " + category.getName() + "s."); return;
        }
        new AccountPunishmentsMessage(sender.getId(), target.getId(), category).send();
    }

    private void handleGolds(BungeeCommandContext context, CommandSender sender, Account target, String[] args, String label) {
        if (args.length == 2) {
            sender.send("§cComo usar " + label + " golds:",
                    "§c* " + label + " (jogador) golds add (valor)",
                    "§c* " + label + " (jogador) golds remove (valor)");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "add": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) golds add (valor)."); return; }
                if (!Util.isNumber(args[3])) { sender.send("§cSomente números são válidos."); return; }
                int value = Integer.parseInt(args[3]);
                if (value <= 0) { sender.send("§cO valor não pode ser menor ou igual a zero."); return; }
                target.addGolds(value);
                sender.send("§aVocê adicionou " + Util.formatNumber(value) + " ouros para " + target.getNickname() + ".");
                break;
            }
            case "remove": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) golds remove (valor)."); return; }
                if (!Util.isNumber(args[3])) { sender.send("§cSomente números são válidos."); return; }
                int value = Integer.parseInt(args[3]);
                if (value <= 0) { sender.send("§cO valor não pode ser menor ou igual a zero."); return; }
                target.removeGolds(value);
                sender.send("§eVocê removeu " + Util.formatNumber(value) + " ouros de " + target.getNickname() + ".");
                break;
            }
        }
    }

    private void handleClanTag(CommandSender sender, Account target, String[] args, String label) {
        if (args.length == 2) {
            sender.send("§cComo usar " + label + " clantag:",
                    "§c* " + label + " (jogador) clantag set (clantag)",
                    "§c* " + label + " (jogador) clantag remove",
                    "§c* " + label + " (jogador) clantag list");
            return;
        }
        switch (args[2].toLowerCase()) {
            case "set": {
                if (args.length == 3) { sender.send("§cUso: " + label + " (jogador) clantag set (clantag)."); return; }
                ClanTag clanTag = ClanTag.of(args[3]);
                if (clanTag == null || clanTag == ClanTag.NONE) { sender.send("§cClanTag não encontrada."); return; }
                target.setActiveClanTag(clanTag);
                sender.send("§aClanTag " + clanTag.getColoredName() + " §adefinida para " + target.getNickname() + ".");
                log(sender, sender.getName() + " definiu a clantag " + clanTag.getName() + " para " + target.getNickname());
                break;
            }
            case "remove": {
                target.setActiveClanTag(ClanTag.NONE);
                sender.send("§eClanTag de " + target.getNickname() + " removida.");
                break;
            }
            case "list": {
                sender.send("§aClanTags disponíveis:");
                for (ClanTag ct : ClanTag.values()) {
                    if (ct == ClanTag.NONE) continue;
                    sender.send(" §7- " + ct.getColoredName() + " §8| §7Cor: " + ct.getColorCode() + ct.getName() + " §8| §7Permissão: §f" + ct.getPermission());
                }
                break;
            }
        }
    }

    protected void sendInfoMessage(CommandSender sender, Account account) {
        List<String> message = new ArrayList<>(Arrays.asList(
                "§aUsuário: §f" + account.getName(),
                "§aTipo: §f" + (account.isPremium() ? "Original" : "Pirata"),
                "§aNível: §f" + account.getLevel(),
                "§aOuros: §f" + Util.formatNumber(account.getGolds()),
                "§aPrimeira entrada: §f" + DateUtil.getDate(account.getContext().getCreatedAt()),
                "§aÚltima entrada: §f" + DateUtil.getDate(account.getContext().getLastLogin()),
                "§aTag: " + account.getTag().getColoredName(),
                "§aMedalha: " + account.getMedal().getColoredName()
        ));

        for (Rank rank : account.getAvailableRanks()) {
            message.add("§aRank extra: " + rank.getColoredName()
                    + " §8| §7Autor: " + rank.getAuthorName()
                    + (rank.isPermanent() ? "" : " §8| §7Expira: " + DateUtil.getDate(rank.getExpiresAt())));
        }

        message.add("§aRank: " + account.getRank().getColoredName()
                + " §8| §7Autor: " + account.getRank().getAuthorName()
                + " §8| §7Data: " + DateUtil.getDate(account.getRank().getAssignedAt()));

        CompletableFuture.runAsync(() -> {
            GeographyBuilder geography = GeographyService.getGeography(account.getIpAddress());
            if (geography.isValid()) {
                message.add("§aLocalização:");
                message.add(" §7País: " + geography.getCountry());
                message.add(" §7Estado: " + geography.getRegion());
                message.add(" §7Cidade: " + geography.getCity());
                message.add(" §7ASN: " + geography.getAsn());
            }
        });

        sender.send(message);
    }
}
