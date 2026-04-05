package com.minecraft.savagemc.core.bungee.command;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentReason;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.bungee.service.webhook.DiscordWebhookService;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.CommandSender;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class PunishmentCommands implements CommandInheritor {

    private final DiscordWebhookService webhookService = new DiscordWebhookService();

    @Command(name = "unban", rank = RankType.TRIAL, onlyPlayer = false)
    public void unban(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 1) {
            sender.send("§cUso: /unban (jogador) [motivo]");
            return;
        }

        Account target = context.getAccount(args[0]);
        if (target == null) {
            target = Savage.getAccountData().of(args[0]);
        }
        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        Punishment punishment = Savage.getPunishmentData().of(target.getId(), PunishmentCategory.BAN);
        if (punishment == null) {
            sender.send("§cO jogador não possui um banimento ativo.");
            return;
        }

        String reason = args.length > 1 ? context.getMessage(1, args) : "Sem motivo especificado";
        punishment.revoke(sender.getId(), reason);
        Savage.getPunishmentData().update(punishment, "revocation");

        target.updatePunishment(punishment);

        sender.send("§aVocê desbaniu o jogador " + target.getName() + ".");
        log(sender, sender.getName() + " desbaniu " + target.getName() + " - Motivo: " + reason);
        webhookService.sendUnpunishmentLog("unban", sender.getName(), target.getName(), reason, PunishmentCategory.BAN);
    }

    @Command(name = "unmute", rank = RankType.TRIAL, onlyPlayer = false)
    public void unmute(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 1) {
            sender.send("§cUso: /unmute (jogador) [motivo]");
            return;
        }

        Account target = context.getAccount(args[0]);
        if (target == null) {
            target = Savage.getAccountData().of(args[0]);
        }
        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        Punishment punishment = Savage.getPunishmentData().of(target.getId(), PunishmentCategory.MUTE);
        if (punishment == null) {
            sender.send("§cO jogador não possui um mute ativo.");
            return;
        }

        String reason = args.length > 1 ? context.getMessage(1, args) : "Sem motivo especificado";
        punishment.revoke(sender.getId(), reason);
        Savage.getPunishmentData().update(punishment, "revocation");

        target.updatePunishment(punishment);

        if (target.proxiedPlayer() != null) {
            target.send("§aVocê foi desmutado por " + sender.getName() + ".");
        }

        sender.send("§aVocê desmutou o jogador " + target.getName() + ".");
        log(sender, sender.getName() + " desmutou " + target.getName() + " - Motivo: " + reason);
        webhookService.sendUnpunishmentLog("unmute", sender.getName(), target.getName(), reason, PunishmentCategory.MUTE);
    }

    @Command(name = "ipban", rank = RankType.TRIAL, onlyPlayer = false)
    public void ipban(BungeeCommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArgs();

        if (args.length < 2) {
            sender.send("§cUso: /ipban (jogador/ip) (tempo) [motivo]");
            return;
        }

        String targetArg = args[0];
        String timeArg = args[1];
        boolean isValidTime = TimeUtil.isValidTime(timeArg);
        
        String ip = targetArg;
        Account target = null;
        
        if (!targetArg.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            target = context.getAccount(targetArg);
            if (target == null) {
                target = Savage.getAccountData().of(targetArg);
            }
            if (target == null) {
                sender.send(TARGET_NOT_FOUND);
                return;
            }
            ip = target.getIpAddress();
        }

        if (ip == null || ip.isEmpty()) {
            sender.send("§cNão foi possível obter o IP do jogador.");
            return;
        }

        String targetName = target != null ? target.getName() : ip;

        long expiresAt = isValidTime ? TimeUtil.getTime(timeArg) : -1L;
        String motive = isValidTime && args.length > 2 ? context.getMessage(2, args) : 
                       !isValidTime && args.length > 1 ? context.getMessage(1, args) : "Banimento por IP";

        Punishment punishment = Punishment.builder()
                .author(sender.getId())
                .player(target != null ? target.getId() : Constant.DEFAULT_ID)
                .server(target != null ? target.getServerType() : null)
                .arcade(ArcadeCategory.NONE)
                .ip(ip)
                .reason(PunishmentReason.IP_BAN)
                .category(PunishmentCategory.BAN)
                .cause(motive)
                .expiresAt(expiresAt)
                .build();

        Savage.getPunishmentData().save(punishment);

        if (target != null && target.proxiedPlayer() != null) {
            target.proxiedPlayer().disconnect(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                    punishment.isTemporary() ? "temporariamente" : "permanentemente",
                    PunishmentReason.IP_BAN.getName(),
                    punishment.getId())));
        }

        sender.send("§cVocê baniu o IP " + ip + " por " + PunishmentReason.IP_BAN.getName() + ".");
        log(sender, sender.getName() + " baniu o IP " + ip + " - Motivo: " + motive);
        webhookService.sendPunishmentLog(punishment, "ipban", sender.getName(), targetName);
    }

    @Command(name = "punir", aliases = {"punish", "p"}, rank = RankType.TRIAL, onlyPlayer = false)
    public void punish(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length < 4) {
            sender.send("§cUso: /" + context.getLabel() + " (categoria) (causa) (tempo) (jogador) (motivo).");
            return;
        }

        PunishmentCategory category = PunishmentCategory.of(args[0]);

        if (category == null) {
            sender.send("§cA categoria solicitada não foi encontrada.");
            return;
        }

        PunishmentReason reason = PunishmentReason.of(args[1]);

        if (reason == null) {
            sender.send("§cA causa solicitada não foi encontrada.");
            return;
        }

        long expiresAt = -1L;

        boolean isValidTime = TimeUtil.isValidTime(args[2]);

        if (isValidTime)
            expiresAt = TimeUtil.getTime(args[2]);

        Account target = context.getAccount(args[isValidTime ? 3 : 2]);
        
        if (target == null) {
            target = Savage.getAccountData().of(args[isValidTime ? 3 : 2]);
        }

        if (target == null) {
            sender.send(TARGET_NOT_FOUND);
            return;
        }

        if (sender.getId().equals(target.getId())) {
            sender.send("§cVocê não pode punir a si mesmo.");
            return;
        }

        if (sender.isPlayer()) {
            Account account = context.getAccount();

            if (!account.hasRank(target.getRankType()) || account.hasOnlyRank(target.getRankType())) {
                sender.send("§cVocê não pode punir alguém superior ou igual a você.");
                return;
            }
        }

        if (target.getActivePunishment(category) != null) {
            sender.send("§cO jogador " + target.getName() + " já possui uma punição em aberto.");
            return;
        }

        String motive = context.getMessage(isValidTime ? 4 : 3, args);

        Punishment punishment = Punishment.builder()
                .author(sender.getId())
                .player(target.getId())
                .server(target.getServerType())
                .arcade(target.getRoute().isValidArcade() ? target.getArcadeRoute().getArcade() : ArcadeCategory.NONE)
                .ip(target.getIpAddress())
                .reason(reason)
                .category(category)
                .cause(motive)
                .expiresAt(expiresAt)
                .build();

        Savage.getPunishmentData().save(punishment);

        if (category.equals(PunishmentCategory.BAN) && target.proxiedPlayer() != null)
            target.proxiedPlayer().disconnect(TextComponent.fromLegacyText(String.format(Constant.BAN_TEMPLATE_MESSAGE,
                    punishment.isTemporary() ? "temporariamente" : "permanentemente",
                    reason.getName(),
                    punishment.getId())));
        else if (category.equals(PunishmentCategory.MUTE))
            target.send("§cVocê foi " + (punishment.isTemporary() ? "temporariamente" : "permanentemente") + " mutado no servidor.");

        sender.send("§cVocê puniu o jogador " + target.getName() + " por " + reason.getName() + ".");

        log(sender, sender.getName() + " puniu " + target.getName() + " por " + motive + " (" + category.getName() + "/" + reason.getName() + ")");
        webhookService.sendPunishmentLog(punishment, "punish", sender.getName(), target.getName());
    }

    @Command(name = "cban", permission = "cban", onlyPlayer = false)
    public void cheatingBan(BungeeCommandContext context) {
        CommandSender sender = context.getSender();

        String[] args = context.getArgs();

        if (args.length <= 1) {
            sender.send("§cUso: /" + context.getLabel() + " (jogador) (motivo).");
            return;
        }

        ProxyServer.getInstance().getPluginManager().dispatchCommand(sender.isPlayer() ? context.getPlayer() : ProxyServer.getInstance().getConsole(),
                String.format("punish %s %s %s %s",
                        PunishmentCategory.BAN.name(),
                        PunishmentReason.CHEATING.name(),
                        args[0],
                        context.getMessage(1, args)));
    }
}
