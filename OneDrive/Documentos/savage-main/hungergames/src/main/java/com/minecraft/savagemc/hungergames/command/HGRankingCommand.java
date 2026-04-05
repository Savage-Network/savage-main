package com.minecraft.savagemc.hungergames.command;

import com.minecraft.core.account.Account;
import com.minecraft.core.command.annotation.Command;
import com.minecraft.core.command.command.Context;
import com.minecraft.core.command.platform.Platform;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.enums.Ranking;
import org.bukkit.entity.Player;

public class HGRankingCommand {

    @Command(name = "ranking", platform = Platform.PLAYER)
    public void handleHGRanking(Context<Player> context) {
        Player player = context.getSender();
        Account account = context.getAccount();

        player.sendMessage("§aCarregando...");
        
        // Executar de forma assíncrona
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
            org.bukkit.Bukkit.getPluginManager().getPlugin("HungerGames"),
            () -> {
                // Carregar dados se necessário
                account.getDataStorage().loadIfUnloaded(Columns.HG_RANK, Columns.HG_RANK_EXP);

                // Pegar rank e XP atual
                int rankId = account.getData(Columns.HG_RANK).getAsInt();
                int xp = account.getData(Columns.HG_RANK_EXP).getAsInt();
                
                Ranking currentRank = Ranking.fromId(rankId);
                if (currentRank == null) {
                    currentRank = Ranking.BRONZE_I;
                }
                
                Ranking nextRank = currentRank.getNextRanking();
                
                // Pegar posição no rank
                int position = getRankPosition(currentRank, xp);
                String positionSuffix = getPositionSuffix(position);

                // Voltar para thread principal
                Ranking finalCurrentRank = currentRank;
                Ranking finalNextRank = nextRank;
                org.bukkit.Bukkit.getScheduler().runTask(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("HungerGames"),
                    () -> {
                        player.sendMessage("");
                        player.sendMessage("§c§lRANKING HUNGERGAMES");
                        player.sendMessage("");

                        // Mostrar todos os ranks
                        for (Ranking rank : Ranking.getValues()) {
                            String suffix = "";
                            
                            // Destacar rank atual com posição
                            if (rank == finalCurrentRank) {
                                String positionText = position >= 1000 ? "1k+" : position + positionSuffix;
                                suffix = " §e(Atual - " + positionText + ")";
                            }
                            // Destacar próximo rank
                            else if (rank == finalNextRank && finalCurrentRank != Ranking.CHALLENGER) {
                                suffix = " §6(Próximo)";
                            }
                            
                            // Formatar linha com símbolo (sem pts)
                            String displayName = rank.getColor() + rank.getSymbol() + " " + rank.getColor() + rank.getName();
                            String line = "§7- " + displayName + suffix;
                            player.sendMessage(line);
                        }

                        player.sendMessage("");
                        
                        // Mostrar progresso
                        player.sendMessage("§7Seu XP: §b" + xp + " pts");
                        if (finalCurrentRank != Ranking.CHALLENGER) {
                            int xpNeeded = finalNextRank.getExperience() - xp;
                            if (xpNeeded > 0) {
                                player.sendMessage("§7Faltam §b" + xpNeeded + " XP §7para " + finalNextRank.getColor() + finalNextRank.getName());
                            }
                        } else {
                            player.sendMessage("§a§lVocê atingiu o rank máximo!");
                        }
                        
                        player.sendMessage("");
                    }
                );
            }
        );
    }
    
    private int getRankPosition(Ranking rank, int xp) {
        try {
            // Query para contar quantos jogadores têm mais XP no mesmo rank
            String sql = "SELECT COUNT(*) as count FROM hungergames WHERE hg_rank = ? AND hg_rank_exp > ?";
            java.sql.PreparedStatement ps = com.minecraft.core.Constants.getMySQL().getConnection().prepareStatement(sql);
            ps.setInt(1, rank.getId());
            ps.setInt(2, xp);
            
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int position = rs.getInt("count") + 1;
                rs.close();
                ps.close();
                return position;
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 1;
    }
    
    private String getPositionSuffix(int position) {
        return "º";
    }
}
