package com.minecraft.savagemc.core.bukkit.manager.list;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.account.context.objects.medal.Medal;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MedalManager {

    public static List<Medal> getAvailableMedals(Account account) {
        return Stream.of(Medal.values())
                .filter(m -> m != Medal.NONE && account.hasMedal(m))
                .collect(Collectors.toList());
    }

    public static void sendMedals(Account account) {
        List<Medal> medals = getAvailableMedals(account);

        if (medals.isEmpty()) {
            account.send("§cVocê ainda não possui medalhas ;(",
                    "§cAdquira medalhas em: §e" + Constant.SERVER_STORE);
            return;
        }

        TextComponent message = new TextComponent("§aSuas medalhas: ");

        int index = 1;
        for (Medal medal : medals) {
            boolean end = index >= medals.size();
            boolean using = account.isUsingMedal(medal);

            TextComponent component = new TextComponent(medal.getColoredSymbol() + (using ? " §a✔" : ""));

            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                    medal.getColoredName() + (using ? "\n§aSelecionada atualmente!" : "\n§eClique para selecionar")
            )));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/medal " + medal.getName()));

            message.addExtra(component);
            message.addExtra("§r" + (end ? "." : ", "));

            index++;
        }

        account.send(message);
    }
}
