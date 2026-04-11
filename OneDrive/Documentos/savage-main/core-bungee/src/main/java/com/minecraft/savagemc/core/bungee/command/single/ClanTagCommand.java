package com.minecraft.savagemc.core.bungee.command.single;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.clantag.ClanTag;
import com.minecraft.savagemc.core.api.clan.Clan;
import com.minecraft.savagemc.core.bungee.command.structure.BungeeCommandContext;
import com.minecraft.savagemc.core.command.CommandInheritor;
import com.minecraft.savagemc.core.command.annotation.Command;
import com.minecraft.savagemc.core.command.annotation.Completer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClanTagCommand implements CommandInheritor {

    @Completer(name = "clan.tag")
    public List<String> completer(BungeeCommandContext context) {
        String[] args = context.getArgs();
        if (args.length == 1)
            return Arrays.stream(ClanTag.values())
                    .filter(ct -> ct != ClanTag.NONE)
                    .map(ct -> ct.name().toLowerCase())
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        return Collections.emptyList();
    }

    @Command(name = "clan.tag")
    public void clanTag(BungeeCommandContext context) {
        Account account = context.getAccount();
        String[] args = context.getArgs();

        Clan clan = account.getClan();

        if (clan == null) {
            account.send("§cVocê precisa estar em um clan para usar este comando.");
            return;
        }

        // Sem argumentos: mostra a clan tag atual e as disponíveis
        if (args.length == 0) {
            ClanTag current = account.getContext().getClanTag();

            if (current == null || current == ClanTag.NONE) {
                account.send("§eVocê não tem nenhuma clan tag selecionada.");
            } else {
                account.send("§eSua clan tag atual: " + formatTag(current, clan.getTag()));
            }

            account.send("");
            account.send("§eCores disponíveis: §7(clique para selecionar)");

            for (ClanTag ct : ClanTag.values()) {
                if (ct == ClanTag.NONE) continue;

                boolean selected = ct == current;
                String coloredName = ct.getColoredName() + (selected ? " §a✔" : "");
                String preview = "§7Preview: " + formatTag(ct, clan.getTag());

                TextComponent component = new TextComponent(TextComponent.fromLegacyText(coloredName));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        TextComponent.fromLegacyText(preview + (selected ? "\n§aSelecionada atualmente!" : "\n§eClique para selecionar"))));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/clan tag " + ct.name().toLowerCase()));

                context.getPlayer().sendMessage(component);
            }
            return;
        }

        // Com argumento: seleciona a cor
        ClanTag selected = ClanTag.of(args[0]);

        if (selected == null || selected == ClanTag.NONE) {
            account.send("§cCor inválida. Use §e/clan tag §cpara ver as disponíveis.");
            return;
        }

        // Verifica permissão (staff bypassa)
        if (selected.getPermission() != null && !account.isStaffer() && !account.hasPermission(selected.getPermission())) {
            account.send("§cVocê não tem permissão para usar esta clan tag.");
            return;
        }

        account.getContext().setClanTag(selected);
        Savage.getAccountData().update(account, "context");

        account.send("§aClan tag definida para: " + formatTag(selected, clan.getTag()));
    }

    private String formatTag(ClanTag ct, String tag) {
        if (ct == null || ct == ClanTag.NONE)
            return "§7[" + tag.toUpperCase() + "]";
        return ct.format("[" + tag.toUpperCase() + "]");
    }
}

