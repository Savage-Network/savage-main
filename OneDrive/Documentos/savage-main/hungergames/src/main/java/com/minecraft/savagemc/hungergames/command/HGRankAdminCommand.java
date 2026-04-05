package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.annotation.Completer;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Rank;
import com.minecraft.core.enums.Ranking;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HGRankAdminCommand {

    @Command(name = "hgrank", usage = "<player> <rank> [xp]", platform = Platform.BOTH, rank = Rank.ADMINISTRATOR)
    public void handleSetRank(Context<CommandSender> context, Account target, Ranking ranking) {
        if (target == null) {
            context.info("account.not_found");
            return;
        }

        if (ranking == null) {
            context.sendMessage("§cRank inválido! Use: " + Arrays.stream(Ranking.getValues())
                    .map(Ranking::getName)
                    .collect(Collectors.joining(", ")));
            return;
        }

        // Definir rank e XP mínimo do rank
        target.getData(Columns.HG_RANK).setData(ranking.getId());
        target.getData(Columns.HG_RANK_EXP).setData(ranking.getExperience());
        
        target.getDataStorage().saveColumn(Columns.HG_RANK);
        target.getDataStorage().saveColumn(Columns.HG_RANK_EXP);

        context.sendMessage("§aRank de §f" + target.getUsername() + " §aalterado para " + 
                ranking.getColor() + ranking.getSymbol() + " " + ranking.getName() + 
                " §a(" + ranking.getExperience() + " XP)");
        
        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player != null && player.isOnline()) {
            player.sendMessage("§aSeu rank HG foi alterado para " + 
                    ranking.getColor() + ranking.getSymbol() + " " + ranking.getName());
        }
    }

    @Command(name = "hgrank", usage = "<player> <rank> <xp>", platform = Platform.BOTH, rank = Rank.ADMINISTRATOR)
    public void handleSetRankWithXP(Context<CommandSender> context, Account target, Ranking ranking, Integer xp) {
        if (target == null) {
            context.info("account.not_found");
            return;
        }

        if (ranking == null) {
            context.sendMessage("§cRank inválido! Use: " + Arrays.stream(Ranking.getValues())
                    .map(Ranking::getName)
                    .collect(Collectors.joining(", ")));
            return;
        }

        if (xp < 0) {
            context.sendMessage("§cXP não pode ser negativo!");
            return;
        }

        // Definir rank e XP customizado
        target.getData(Columns.HG_RANK).setData(ranking.getId());
        target.getData(Columns.HG_RANK_EXP).setData(xp);
        
        target.getDataStorage().saveColumn(Columns.HG_RANK);
        target.getDataStorage().saveColumn(Columns.HG_RANK_EXP);

        context.sendMessage("§aRank de §f" + target.getUsername() + " §aalterado para " + 
                ranking.getColor() + ranking.getSymbol() + " " + ranking.getName() + 
                " §a(" + xp + " XP)");
        
        Player player = Bukkit.getPlayer(target.getUniqueId());
        if (player != null && player.isOnline()) {
            player.sendMessage("§aSeu rank HG foi alterado para " + 
                    ranking.getColor() + ranking.getSymbol() + " " + ranking.getName() + 
                    " §7(" + xp + " XP)");
        }
    }

    @Completer(name = "hgrank")
    public List<String> handleComplete(Context<CommandSender> context) {
        if (context.argsCount() == 1) {
            return null; // Lista de jogadores online
        } else if (context.argsCount() == 2) {
            return Arrays.stream(Ranking.getValues())
                    .map(Ranking::getName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
