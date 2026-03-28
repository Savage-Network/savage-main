package com.minecraft.savagemc.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.api.hologram.Hologram;
import com.minecraft.savagemc.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.savagemc.core.bukkit.api.hologram.leaderboard.period.LeaderboardPeriod;
import com.minecraft.savagemc.core.bukkit.api.hologram.row.HologramRow;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.member.Member;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BedLevelLeaderboard extends LeaderboardHologram {

    public BedLevelLeaderboard(Player player, Location location) {
        super("level", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage); // 100 items, 10 per page

        // Retrieve the page number from the hologram, default to 0 if not set
        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        LeaderboardPeriod period = (LeaderboardPeriod) hologram.getProperty("period", LeaderboardPeriod.GENERAL);

        // Fetch top 100 players
        Stream<BedMember> stream = Savage.getBedWarsData().ranking("context.level", 100).stream();

        if (period.equals(LeaderboardPeriod.WEEK))
            stream = stream.sorted(Comparator.comparing(Member::getWeekLevelXp));
        else if (period.equals(LeaderboardPeriod.MONTH))
            stream = stream.sorted(Comparator.comparing(Member::getMonthLevelXp));

        stream = stream.filter(member -> Savage.getAccountData().of(member.getId()) != null);

        List<BedMember> members = stream.collect(Collectors.toList());

        // Add header
        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lNÍVEL",
                ""
        ));

        // Calculate start and end indices for the current page
        int start = currentPage * itemsPerPage;

        // Convert Collection to List for easier access by index
        List<BedMember> memberList = new ArrayList<>(members);

        for (int i = start; i < start + itemsPerPage; i++) {
            if (i < memberList.size()) {
                BedMember member = memberList.get(i);

                Account account = Savage.getAccountData().of(member.getId());

                lines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getRank().getColor() + account.getName() + (account.hasClan() ? " §8[" + account.getClanTag() + "]" : ""),
                        member.getLevelId()
                ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        // Add footer
        lines.addAll(Arrays.asList(
                "§6§lClique para trocar!",
                handlePeriodLine(period)
        ));

        hologram.setSpaciousText(lines);

        // Handle touch events to navigate pages
        hologram.setTouch((player, touch) -> {
            int nextPage = (hologram.getInt("page") + 1) % totalPages;
            hologram.writeProperty("page", nextPage);

            updateHologram(memberList, itemsPerPage, totalPages, nextPage);
            player.playSound(Sound.CLICK);
        });

        HologramRow periodRow = hologram.getLastRow();

        periodRow.setTouch((player, touch) -> {
            LeaderboardPeriod next = ((LeaderboardPeriod) hologram.getProperty("period", LeaderboardPeriod.GENERAL)).next();

            hologram.writeProperty("period", next);

            player.playSound(Sound.CLICK);
            handle();
        });
    }

    // Method to update hologram text based on page
    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        LeaderboardPeriod period = (LeaderboardPeriod) hologram.getProperty("period", LeaderboardPeriod.GENERAL);

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lNÍVEL",
                ""));

        int startIdx = nextPage * itemsPerPage;

        for (int i = startIdx; i < startIdx + itemsPerPage; i++) {
            if (i < memberList.size()) {
                BedMember member = memberList.get(i);
                Account account = Savage.getAccountData().of(member.getId());

                if (account == null) {
                    newLines.add(String.format("§e%s. ...", i + 1));
                    continue;
                }

                newLines.add(String.format("§e%s. %s §7- %s",
                        i + 1,
                        account.getRank().getColor() + account.getName() + (account.hasClan() ? " §8[" + account.getClanTag() + "]" : ""),
                        period.equals(LeaderboardPeriod.GENERAL)
                                ? member.getLevelId()
                                : period.equals(LeaderboardPeriod.WEEK)
                                ? "§7" + Util.formatNumberWithLetter(member.getWeekLevelXp())
                                : "§7" + Util.formatNumberWithLetter(member.getMonthLevelXp())
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.addAll(Arrays.asList(
                "§6§lClique para trocar!",
                handlePeriodLine(period)
        ));

        hologram.setSpaciousText(newLines);
    }

    protected String handlePeriodLine(LeaderboardPeriod period) {
        StringBuilder builder = new StringBuilder();

        int index = 0;
        boolean end = false;
        for (LeaderboardPeriod search : LeaderboardPeriod.values()) {
            if (index >= LeaderboardPeriod.values().length) end = true;

            boolean using = period.equals(search);

            builder.append((using ? "§a§l" : "§7")).append(search.getName())
                    .append(end ? "" : " ");

            index++;
        }

        return builder.toString();
    }
}
