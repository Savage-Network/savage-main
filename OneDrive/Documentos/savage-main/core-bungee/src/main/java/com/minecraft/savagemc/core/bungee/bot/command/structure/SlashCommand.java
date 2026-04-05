package com.minecraft.savagemc.core.bungee.bot.command.structure;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.bungee.BungeeCore;
import com.minecraft.savagemc.core.bungee.bot.Bot;
import com.minecraft.savagemc.core.bungee.bot.command.structure.category.SlashCommandCategory;
import lombok.Getter;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public abstract class SlashCommand {

    private final String name;
    private final String description;
    private final SlashCommandCategory category;
    private final List<SlashCommandOption> options = new ArrayList<>();

    public SlashCommand(String name, String description, SlashCommandCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public abstract void execute(SlashCommandInteraction interaction);

    public SlashCommandBuilder builder() {
        SlashCommandBuilder builder = new SlashCommandBuilder()
                .setName(name)
                .setDescription(description);

        options.forEach(builder::addOption);

        Bot bot = BungeeCore.getInstance().getBot();

        if (bot == null || bot.getApi() == null) {
            Savage.getLogger().warning("[Slash] Bot offline, comando \"" + name + "\" não registrado no Discord.");
            return builder;
        }

        if (category.equals(SlashCommandCategory.GLOBAL))
            builder.createGlobal(bot.getApi())
                    .thenAccept(command -> Savage.getLogger().info("[Slash/Global] Comando \"" + command.getName() + "\" registrado."))
                    .exceptionally(e -> {
                        Savage.getLogger().log(Level.SEVERE, "[Slash/Global] Não foi possível rodar o comando...", e);
                        return null;
                    });
        else {
            if (bot.getServer() == null) {
                Savage.getLogger().warning("[Slash] Servidor Discord não encontrado, comando \"" + name + "\" não registrado.");
                return builder;
            }

            builder.createForServer(bot.getServer())
                    .thenAccept(command -> Savage.getLogger().info("[Slash/Server] Comando \"" + command.getName() + "\" registrado."))
                    .exceptionally(e -> {
                        Savage.getLogger().log(Level.SEVERE, "[Slash/Server] Não foi possível rodar o comando...", e);
                        return null;
                    });
        }

        return builder;
    }
}
