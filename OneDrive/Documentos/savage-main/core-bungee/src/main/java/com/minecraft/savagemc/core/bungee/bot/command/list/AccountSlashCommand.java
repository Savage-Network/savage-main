package com.minecraft.savagemc.core.bungee.bot.command.list;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.punishment.Punishment;
import com.minecraft.savagemc.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.savagemc.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.savagemc.core.bungee.bot.command.structure.category.SlashCommandCategory;
import com.minecraft.savagemc.core.bungee.bot.command.structure.embed.SlashEmbed;
import com.minecraft.savagemc.core.util.list.DateUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.List;
import java.util.stream.Collectors;

public class AccountSlashCommand extends SlashCommand {

    private final String NAME_OPTION_KEY = "nome";

    public AccountSlashCommand() {
        super("conta", "Colete informações dos jogadores.", SlashCommandCategory.SERVER);

        getOptions().add(
                new SlashCommandOptionBuilder()
                        .setType(SlashCommandOptionType.STRING)
                        .setName(NAME_OPTION_KEY)
                        .setDescription("Específique a conta.")
                        .setRequired(true)
                        .build()
        );
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        SlashCommandInteractionOption optionName = interaction.getArgumentByName(NAME_OPTION_KEY).orElse(null);

        if (optionName == null) return;

        String name = optionName.getStringValue().orElse("...");
        Account account = Savage.getAccountData().of(name);

        if (account == null) {
            interaction.createImmediateResponder()
                    .setContent("Não foi possível encontrar a conta \"" + name + "\"...")
                    .respond();
            return;
        }

        // Contas passadas (alts pelo mesmo IP)
        List<Account> alts = Savage.getAccountData().getAlts(account.getContext().getIpAddress())
                .stream()
                .filter(a -> !a.getId().equals(account.getId()))
                .collect(Collectors.toList());

        String altNames = alts.isEmpty()
                ? "Nenhuma"
                : alts.stream().map(Account::getName).collect(Collectors.joining(", "));

        // Banimentos
        List<Punishment> bans = account.getPunishments(PunishmentCategory.BAN);
        String bansText = bans.isEmpty()
                ? "Nenhum"
                : bans.stream()
                    .map(p -> String.format("**%s** — %s (%s)",
                            p.getReason().name(),
                            p.getCause(),
                            p.isValid() ? "Ativo" : "Revogado"))
                    .collect(Collectors.joining("\n"));

        // Mutes
        List<Punishment> mutes = account.getPunishments(PunishmentCategory.MUTE);
        String mutesText = mutes.isEmpty()
                ? "Nenhum"
                : mutes.stream()
                    .map(p -> String.format("**%s** — %s (%s)",
                            p.getReason().name(),
                            p.getCause(),
                            p.isValid() ? "Ativo" : "Revogado"))
                    .collect(Collectors.joining("\n"));

        EmbedBuilder embed = new SlashEmbed()
                .setTitle("Informações de conta")
                .setThumbnail(String.format(Constant.MINOTAR_AVATAR_API_URL, account.getName(), 120))
                .addField("Nome", account.getName(), true)
                .addField("Rank", account.getRank().getName(), true)
                .addField("Entrou em", DateUtil.getDate(account.getContext().getCreatedAt()), true)
                .addField("Último Login", DateUtil.getDate(account.getContext().getLastLogin()), true)
                .addField("Contas passadas", altNames, false)
                .addField("Banimentos (" + bans.size() + ")", bansText, false)
                .addField("Mutes (" + mutes.size() + ")", mutesText, false);

        interaction.createImmediateResponder()
                .addEmbed(embed)
                .respond();
    }
}
