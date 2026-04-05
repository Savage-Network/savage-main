package com.minecraft.savagemc.lobby.architect.list.bedwars.leaderboard;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.bukkit.api.hologram.leaderboard.LeaderboardHologram;
import com.minecraft.savagemc.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.util.Util;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BedWinsLeaderboard extends LeaderboardHologram {

    public BedWinsLeaderboard(Player player, Location location) {
        super("wins", player, location, 10);
    }

    @Override
    public void handle() {
        HologramClient hologram = getHologram();

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil(100.0 / itemsPerPage); // 100 items, 10 per page

        // Retrieve the page number from the hologram, default to 0 if not set
        hologram.writeProperty("page", 0);

        int currentPage = hologram.getInt("page");

        // Fetch top 100 players
        List<BedMember> members = Savage.getBedWarsData().ranking("metadata.statsList", 100)
                .stream()
                .filter(member -> Savage.getAccountData().of(member.getId()) != null)
                .sorted((a, b) -> Integer.compare(b.getTotalWins(), a.getTotalWins())) // Ordem decrescente
                .collect(Collectors.toList());

        // Add header
        List<String> lines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", currentPage + 1, totalPages),
                "§e§lVITÓRIAS",
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
                        Util.formatNumber(member.getTotalWins())
                ));
            } else {
                lines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        // Add footer
        lines.add("§6§lClique para trocar!");

        hologram.setSpaciousText(lines);

        // Handle touch events to navigate pages
        hologram.setTouch((player, touch) -> {
            int nextPage = (hologram.getInt("page") + 1) % totalPages; // Cycle to the next page, wrapping around after the last page
            hologram.writeProperty("page", nextPage); // Store the new page number

            updateHologram(memberList, itemsPerPage, totalPages, nextPage); // Update the hologram text for the new page
            player.playSound(Sound.CLICK);
        });
    }

    // Method to update hologram text based on page
    private void updateHologram(List<BedMember> memberList, int itemsPerPage, int totalPages, int nextPage) {
        HologramClient hologram = getHologram();

        List<String> newLines = new ArrayList<>(Arrays.asList(
                String.format("§b§lTOP 100 §7(%d/%d)", nextPage + 1, totalPages),
                "§e§lVITÓRIAS",
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
                        Util.formatNumber(member.getTotalWins())
                ));
            } else {
                newLines.add(String.format("§e%s. §7...", i + 1));
            }
        }

        newLines.add("§6§lClique para trocar!");

        hologram.setSpaciousText(newLines);
    }
}

