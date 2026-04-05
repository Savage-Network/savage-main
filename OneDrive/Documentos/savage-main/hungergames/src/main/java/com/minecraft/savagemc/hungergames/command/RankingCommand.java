/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.command;

import com.google.common.base.Strings;
import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.enums.Ranking;
import com.minecraft.core.util.ranking.RankingTarget;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RankingCommand implements Assistance {

    @Command(name = "ranking", platform = Platform.PLAYER, aliases = "rank")
    public void rankingCommand(Context<Player> context) {
        Account account = context.getAccount();
        RankingTarget target = getPlugin().getRankingFactory().getTarget();

        Ranking ranking = account.getRanking();
        Ranking next = ranking.getNextRanking();

        int exp = account.getData(target.getExperience()).getAsInt();
        String expFormatted = account.getData(target.getExperience()).getAsInteger();

        context.sendMessage("§aSeu ranking: §f" + ranking.getColor() + ranking.getSymbol() + ' ' + ranking.getName());
        if (next.getId() != ranking.getId() && next.getId() != Ranking.CHALLENGER.getId())
            context.sendMessage("§aPróximo ranking: §f" + next.getColor() + next.getSymbol() + ' ' + next.getName());
        context.sendMessage("§aExpieriência total: §f" + expFormatted);

        if (next.getId() != Ranking.CHALLENGER.getId()) {
            int comparatorExperience = exp - ranking.getExperience();

            String progress = ranking.getColor() + ranking.getDisplay() + ranking.getSymbol() + " " + progress(comparatorExperience, next.getExperience(), 30) + "§r> " + next.getColor() + next.getDisplay() + next.getSymbol() + " §7" + expFormatted + "/" + Constants.DECIMAL_FORMAT.format(next.getExperience()) + " (" + ((exp * 100) / next.getExperience()) + "%)";
            context.sendMessage(progress);
        }
    }

    @Command(name = "ranks", platform = Platform.PLAYER, aliases = "ranklist")
    public void rankingsCommand(Context<Player> context) {
        Map<String, List<Ranking>> grouped = Arrays.stream(Ranking.values())
                .collect(Collectors.groupingBy(r -> r.name().split("_")[0], LinkedHashMap::new, Collectors.toList()));

        StringBuilder builder = new StringBuilder("§aRankings:\n");

        for (Map.Entry<String, List<Ranking>> entry : grouped.entrySet()) {
            List<Ranking> ranks = entry.getValue();

            String category = ranks.get(0).getName().split(" ")[0];

            String levels = ranks.stream()
                    .map(Ranking::getDisplay)
                    .collect(Collectors.joining(", "));

            String color = ranks.get(0).getColor();

            builder.append(color)
                    .append(levels)
                    .append(" §7- ")
                    .append(category)
                    .append("\n");
        }

        context.sendMessage(builder.toString());
    }

    public String progress(int current, int max, int totalBars) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        return Strings.repeat("§a§m ", progressBars)
                + Strings.repeat("§f§m ", totalBars - progressBars);
    }
}
