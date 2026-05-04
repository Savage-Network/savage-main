package com.minecraft.core.bungee.bot.controller;

import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.arcade.category.ArcadeCategory;
import com.minecraft.core.bungee.BungeeCore;
import com.minecraft.core.util.list.TimeUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.IncomingWebhook;

import java.awt.*;

public class BotController {

    private static final String PUBLIC_BAN_WEBHOOK = "https://discord.com/api/webhooks/1500186127116537856/Plnjq6OeXfmBzv_dd89x4y8bUxCavM-ZI4dU0AtpCuHjL-hIq2V-23qG1s7UWFahRK1e";
    private static final String PUBLIC_MUTE_WEBHOOK = "https://discord.com/api/webhooks/1500186127116537856/Plnjq6OeXfmBzv_dd89x4y8bUxCavM-ZI4dU0AtpCuHjL-hIq2V-23qG1s7UWFahRK1e";

    public static void sendPunishmentMessage(Punishment punishment) {
        try {
            if (BungeeCore.getInstance() == null || BungeeCore.getInstance().getBot() == null) {
                return;
            }

            IncomingWebhook webhook = BungeeCore.getInstance().getBot().getWebhook("https://discord.com/api/webhooks/1498739076302373014/JqVeU1qSC7Rj93vRJdGzeHitVdYpyCe08eUQvBZoTw830zDyyNEcGlsdORc1UEP2jfaZ");

            if (webhook != null) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("\uD83D\uDCDB Nova punição!")
                        .addField("ID", punishment.getId())
                        .addInlineField("Autor", punishment.getAuthorName())
                        .addInlineField("Jogador", punishment.getPlayerName())
                        .addInlineField("Categoria", punishment.getCategory().getName())
                        .addField("Motivo", punishment.getReason().getName())
                        .addInlineField("Servidor", punishment.getServer().getName());

                if (!punishment.getArcade().equals(ArcadeCategory.NONE))
                    embed.addInlineField("Modo", punishment.getArcade().getName());

                embed.addInlineField("Expira em", TimeUtil.formatTime(punishment.getExpiresAt(), TimeUtil.TimeFormat.SHORT));
                embed.addField("Motivo", "```" + punishment.getCause() + "```");
                embed.setFooter("Controle de Punições - SavageMC");

                webhook.sendMessage(embed).exceptionally(throwable -> null);
            }
        } catch (Exception ignored) {
        }
    }

    public static void sendRevocationMessage(Punishment punishment) {
        try {
            if (BungeeCore.getInstance() == null || BungeeCore.getInstance().getBot() == null) {
                return;
            }

            IncomingWebhook webhook = BungeeCore.getInstance().getBot().getWebhook("https://discord.com/api/webhooks/1498739076302373014/JqVeU1qSC7Rj93vRJdGzeHitVdYpyCe08eUQvBZoTw830zDyyNEcGlsdORc1UEP2jfaZ");

            if (webhook != null) {
                com.minecraft.core.account.Account revokerAccount = com.minecraft.core.Core.getAccountData().of(punishment.getRevocation().getAuthor());
                String revokerName = revokerAccount != null ? revokerAccount.getName() : "CONSOLE";

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("✅ Punição Revogada")
                        .setColor(Color.PINK)
                        .addField("ID da Punição", punishment.getId())
                        .addInlineField("Jogador", punishment.getPlayerName())
                        .addInlineField("Categoria", punishment.getCategory().getName())
                        .addInlineField("Revogado por", revokerName)
                        .addField("Motivo", "```" + punishment.getRevocation().getReason() + "```");


                webhook.sendMessage(embed).exceptionally(throwable -> null);
            }
        } catch (Exception ignored) {
        }
    }

    public static void sendPublicBanMessage(Punishment punishment) {
        try {
            if (BungeeCore.getInstance() == null || BungeeCore.getInstance().getBot() == null) {
                return;
            }

            // Apenas enviar banimentos para o webhook público
            if (!punishment.getCategory().name().equals("BAN")) {
                return;
            }

            IncomingWebhook webhook = BungeeCore.getInstance().getBot().getWebhook(PUBLIC_BAN_WEBHOOK);

            if (webhook != null) {
                // Obter a skin do jogador
                com.minecraft.core.account.Account playerAccount = com.minecraft.core.Core.getAccountData().of(punishment.getPlayer());
                String playerName = playerAccount != null ? playerAccount.getName() : punishment.getPlayerName();
                
                // URL da cabeça do jogador (usando Crafatar)
                String headUrl = "https://crafatar.com/avatars/" + punishment.getPlayer().toString() + "?size=256&overlay";

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🔨 BANIDO")
                        .setDescription("**" + playerName + "**")
                        .addField("📢 Motivo", punishment.getCause())
                        .setThumbnail(headUrl)
                        .setColor(Color.RED);

                webhook.sendMessage(embed).exceptionally(throwable -> null);
            }
        } catch (Exception ignored) {
        }
    }

    public static void sendPublicMuteMessage(Punishment punishment) {
        try {
            if (BungeeCore.getInstance() == null || BungeeCore.getInstance().getBot() == null) {
                return;
            }

            // Apenas enviar mutes para o webhook público
            if (!punishment.getCategory().name().equals("MUTE")) {
                return;
            }

            IncomingWebhook webhook = BungeeCore.getInstance().getBot().getWebhook(PUBLIC_MUTE_WEBHOOK);

            if (webhook != null) {
                // Obter a skin do jogador
                com.minecraft.core.account.Account playerAccount = com.minecraft.core.Core.getAccountData().of(punishment.getPlayer());
                String playerName = playerAccount != null ? playerAccount.getName() : punishment.getPlayerName();
                
                // URL da cabeça do jogador (usando Crafatar com skin de Enderman para mute)
                String headUrl = "https://crafatar.com/avatars/" + punishment.getPlayer().toString() + "?size=256&overlay";

                // Calcular tempo de expiração
                String expirationText;
                if (punishment.isTemporary()) {
                    long timeLeft = punishment.getExpiresAt() - System.currentTimeMillis();
                    long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(timeLeft);
                    long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(timeLeft) % 24;
                    
                    if (days > 0) {
                        expirationText = days + " dia" + (days > 1 ? "s" : "");
                    } else if (hours > 0) {
                        expirationText = hours + " hora" + (hours > 1 ? "s" : "");
                    } else {
                        long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(timeLeft);
                        expirationText = minutes + " minuto" + (minutes > 1 ? "s" : "");
                    }
                } else {
                    expirationText = "Permanente";
                }

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🔇 SILENCIADO")
                        .setDescription("**" + playerName + "**")
                        .addField("📢 Motivo", punishment.getCause())
                        .addField("⏰ Expira em", expirationText)
                        .setThumbnail(headUrl)
                        .setColor(Color.ORANGE);

                webhook.sendMessage(embed).exceptionally(throwable -> null);
            }
        } catch (Exception ignored) {
        }
    }
}
