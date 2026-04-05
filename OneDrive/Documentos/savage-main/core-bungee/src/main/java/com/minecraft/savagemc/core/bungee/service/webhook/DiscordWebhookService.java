package com.minecraft.savagemc.core.bungee.service.webhook;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.util.list.DateUtil;
import com.minecraft.savagemc.core.util.list.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

public class DiscordWebhookService {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1488643678191747224/XR4zhHSib_zZEsZ8z4D_haC41_ykA0HfriPdch2KXPujX6k5kEIxjlXGTBDvzcdg-7c9";
    private static final String AVATAR_URL = "https://minotar.net/avatar/%s/64.png";

    public void sendPunishmentLog(Punishment punishment, String action, String staffName, String targetName) {
        CompletableFuture.runAsync(() -> {
            try {
                String json = buildPunishmentEmbed(punishment, action, staffName, targetName);
                HttpRequest.post(WEBHOOK_URL)
                    .contentType("application/json")
                    .send(json)
                    .body();
            } catch (Exception e) {
                Savage.getLogger().warning("[Webhook] Falha ao enviar log: " + e.getMessage());
            }
        });
    }

    public void sendUnpunishmentLog(String action, String staffName, String targetName, String reason, PunishmentCategory category) {
        CompletableFuture.runAsync(() -> {
            try {
                String json = buildUnpunishmentEmbed(action, staffName, targetName, reason, category);
                HttpRequest.post(WEBHOOK_URL)
                    .contentType("application/json")
                    .send(json)
                    .body();
            } catch (Exception e) {
                Savage.getLogger().warning("[Webhook] Falha ao enviar log: " + e.getMessage());
            }
        });
    }

    public void sendKickLog(String staffName, String targetName, String reason) {
        CompletableFuture.runAsync(() -> {
            try {
                String json = buildKickEmbed(staffName, targetName, reason);
                HttpRequest.post(WEBHOOK_URL)
                    .contentType("application/json")
                    .send(json)
                    .body();
            } catch (Exception e) {
                Savage.getLogger().warning("[Webhook] Falha ao enviar log: " + e.getMessage());
            }
        });
    }

    public void sendAlert(String title, String description, int color) {
        CompletableFuture.runAsync(() -> {
            try {
                String json = String.format(
                    "{\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d,\"timestamp\":\"%s\"}]}",
                    title, description, color, getCurrentTimestamp()
                );
                HttpRequest.post(WEBHOOK_URL)
                    .contentType("application/json")
                    .send(json)
                    .body();
            } catch (Exception e) {
                Savage.getLogger().warning("[Webhook] Falha ao enviar alerta: " + e.getMessage());
            }
        });
    }

    private String buildPunishmentEmbed(Punishment punishment, String action, String staffName, String targetName) {
        boolean isBan = punishment.getCategory().equals(PunishmentCategory.BAN);
        String emoji = isBan ? "🚫" : "🔇";
        String title = isBan ? "BANIDO" : "MUTADO";
        
        StringBuilder description = new StringBuilder();
        description.append(String.format("## %s\\n\\n", targetName));
        description.append(String.format("📢 **%s**\\n\\n", punishment.getReason().getName()));
        
        if (punishment.isTemporary()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
            String expireDate = sdf.format(new Date(punishment.getExpiresAt()));
            String timeLeft = DateUtil.formatDate(punishment.getExpiresAt() - System.currentTimeMillis(), DateUtil.DateFormat.BIG);
            description.append(String.format("⏰ **Expira em**: \\n%s (%s)", timeLeft, expireDate));
        }
        
        String staffAvatar = String.format(AVATAR_URL, staffName);
        
        return String.format(
            "{\"embeds\":[{\"title\":\"%s %s\",\"description\":\"%s\",\"color\":%d,\"thumbnail\":{\"url\":\"%s\"},\"footer\":{\"text\":\"Por: %s\"},\"timestamp\":\"%s\"}]}",
            emoji, title, description.toString(), isBan ? 15158332 : 16776960, staffAvatar, staffName, getCurrentTimestamp()
        );
    }

    private String buildUnpunishmentEmbed(String action, String staffName, String targetName, String reason, PunishmentCategory category) {
        boolean isUnban = category.equals(PunishmentCategory.BAN);
        String emoji = isUnban ? "✅" : "🔊";
        String title = isUnban ? "DESBANIDO" : "DESMUTADO";
        
        StringBuilder description = new StringBuilder();
        description.append(String.format("## %s\\n\\n", targetName));
        description.append(String.format("📝 **Motivo**: %s", reason));
        
        String staffAvatar = String.format(AVATAR_URL, staffName);
        
        return String.format(
            "{\"embeds\":[{\"title\":\"%s %s\",\"description\":\"%s\",\"color\":%d,\"thumbnail\":{\"url\":\"%s\"},\"footer\":{\"text\":\"Por: %s\"},\"timestamp\":\"%s\"}]}",
            emoji, title, description.toString(), 3066993, staffAvatar, staffName, getCurrentTimestamp()
        );
    }

    private String buildKickEmbed(String staffName, String targetName, String reason) {
        StringBuilder description = new StringBuilder();
        description.append(String.format("## %s\\n\\n", targetName));
        description.append(String.format("📝 **Motivo**: %s", reason));
        
        String staffAvatar = String.format(AVATAR_URL, staffName);
        
        return String.format(
            "{\"embeds\":[{\"title\":\"👢 EXPULSO\",\"description\":\"%s\",\"color\":%d,\"thumbnail\":{\"url\":\"%s\"},\"footer\":{\"text\":\"Por: %s\"},\"timestamp\":\"%s\"}]}",
            description.toString(), 15158332, staffAvatar, staffName, getCurrentTimestamp()
        );
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}
