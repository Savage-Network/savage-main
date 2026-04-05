package com.minecraft.savagemc.hungergames.util.stats;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.clan.Clan;
import com.minecraft.core.database.data.Data;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;
import com.minecraft.core.util.ranking.RankingFactory;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.User;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.sql.SQLException;

@AllArgsConstructor
public enum StatsApplier {

    WIN(user -> {

        if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
            return;

        Account account = user.getAccount();
        user.giveCoins(120.0);

        RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

        if (rankingFactory != null) {
            account.addInt(100, rankingFactory.getTarget().getExperience());
            rankingFactory.verify(account);
            user.getPlayer().sendMessage("§b+100 XP");
        }

        if (account.hasClan()) {
            Clan clan = account.getClan();

            clan.setPoints(clan.getPoints() + 100);
            user.getPlayer().sendMessage("§e+100 pontos §8(Clan)");

            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> {
                try {
                    Constants.getClanService().pushClan(clan);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        account.addInt(1, Columns.HG_WINS);
        Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getEngine(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
    }),

    KILL(user -> {

        if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
            return;

        Account account = user.getAccount();
        user.giveCoins(40.0);

        RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

        if (rankingFactory != null) {
            account.addInt(16, rankingFactory.getTarget().getExperience());
            rankingFactory.verify(account);
            user.getPlayer().sendMessage("§b+16 XP");
        }

        account.addInt(1, Columns.HG_KILLS);

        if (account.hasClan()) {
            Clan clan = account.getClan();

            clan.setPoints(clan.getPoints() + 30);
            user.getPlayer().sendMessage("§e+30 pontos §8(Clan)");

            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> {
                try {
                    Constants.getClanService().pushClan(clan);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        Data data = account.getData(Columns.HG_MAX_GAME_KILLS);

        if (data.getAsInt() < user.getKills())
            data.setData(user.getKills());
    }),

    DEATH(user -> {

        if (!HungerGames.getInstance().getGame().getVariables().isCountStats())
            return;

        Account account = user.getAccount();
        account.addInt(1, Columns.HG_DEATHS);
        account.removeInt(5, Columns.HG_COINS);

        if (account.hasClan()) {
            Clan clan = account.getClan();

            clan.setPoints(clan.getPoints() - 15);
            user.getPlayer().sendMessage("§e-15 pontos §8(Clan)");

            Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), () -> {
                try {
                    Constants.getClanService().pushClan(clan);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        RankingFactory rankingFactory = HungerGames.getEngine().getRankingFactory();

        if (rankingFactory != null) {
            account.removeInt(6, rankingFactory.getTarget().getExperience());
            rankingFactory.verify(account);
            user.getPlayer().sendMessage("§4-6 XP");
        }

        Bukkit.getScheduler().runTaskAsynchronously(HungerGames.getEngine(), () -> account.getDataStorage().saveTable(Tables.HUNGERGAMES));
    });

    @Setter
    private Applier applier;

    public void apply(User user) {
        applier.apply(user);
    }

    public interface Applier {
        void apply(User user);
    }
}
