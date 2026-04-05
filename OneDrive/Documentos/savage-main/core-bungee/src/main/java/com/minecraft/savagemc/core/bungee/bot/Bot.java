package com.minecraft.savagemc.core.bungee.bot;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bungee.bot.command.structure.manager.SlashCommandManager;
import com.minecraft.savagemc.core.bungee.bot.listener.SlashListener;
import lombok.Getter;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.interaction.SlashCommandBuilder;

@Getter
public class Bot {

    private DiscordApi api;

    public Bot() {
        try {
            new DiscordApiBuilder()
                    .setToken(Constant.DISCORD_BOT_TOKEN)
                    .addIntents(Intent.GUILD_MEMBERS, Intent.MESSAGE_CONTENT)
                    .login()
                    .thenAccept(this::handle)
                    .join();
        } catch (Exception e) {
            Savage.getLogger().warning("[Discord] Falha ao conectar o bot: " + e.getMessage());
        }
    }

    public void handle(DiscordApi api) {
        this.api = api;

        api.updateActivity(ActivityType.WATCHING, "Sistemas internos e administrativos.");

        api.addSlashCommandCreateListener(new SlashListener());

        Savage.getLogger().info("[Discord] Os serviços foram iniciados.");
    }

    public void disconnect() {
        if (api != null) {
            api.disconnect().join();

            Savage.getLogger().info("[Discord] Os serviços foram desligados.");
        }
    }

    /* --------------------------
          Métodos Utilitários
    ---------------------------- */
    public IncomingWebhook getWebhook(String link) {
        return api.getIncomingWebhookByUrl(link).join();
    }

    public TextChannel getChannel(String id) {
        return api.getTextChannelById(id).orElse(null);
    }

    public Role getRole(String id) {
        return api.getRoleById(id).orElse(null);
    }

    public Server getServer() {
        return api.getServerById(Constant.DISCORD_SERVER_ID).orElse(null);
    }
}

