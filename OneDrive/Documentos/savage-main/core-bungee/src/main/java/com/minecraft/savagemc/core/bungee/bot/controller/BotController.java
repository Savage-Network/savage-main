package com.minecraft.savagemc.core.bungee.bot.controller;

import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.bungee.BungeeCore;
import com.minecraft.savagemc.core.util.list.TimeUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.IncomingWebhook;

public class BotController {

    public static void sendPunishmentMessage(Punishment punishment) {
        IncomingWebhook webhook = BungeeCore.getInstance().getBot().getWebhook("https://discord.com/api/webhooks/1297209866565320825/EMJ1c0t683i0q4c3lbTx6BxO69E-dkL_tabDM8gucU_DX1we6CS5kMg5AuV3HY4iDDTD");

        if (webhook != null) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("\uD83D\uDCDB Dados da Punição")
                    .addField("ID", punishment.getId())
                    .addInlineField("Autor", punishment.getAuthorName())
                    .addInlineField("Jogador", punishment.getPlayerName())
                    .addInlineField("Categoria", punishment.getCategory().getName())
                    .addField("Causa", punishment.getReason().getName())
                    .addInlineField("Servidor", punishment.getServer().getName());

            if (!punishment.getArcade().equals(ArcadeCategory.NONE))
                embed.addInlineField("Modo", punishment.getArcade().getName());

            embed.addInlineField("Expira em", TimeUtil.formatTime(punishment.getExpiresAt(), TimeUtil.TimeFormat.SHORT));
            embed.addField("Motivo", "```" + punishment.getCause() + "```");

            webhook.sendMessage(embed);
        }
    }
}
