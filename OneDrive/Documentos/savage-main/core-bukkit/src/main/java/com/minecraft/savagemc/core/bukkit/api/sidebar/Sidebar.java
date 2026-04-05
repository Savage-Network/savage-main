package com.minecraft.savagemc.core.bukkit.api.sidebar;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.bukkit.BukkitCore;
import com.minecraft.savagemc.core.bukkit.api.sidebar.row.SidebarRow;
import com.minecraft.savagemc.core.util.Util;
import com.minecraft.savagemc.core.util.list.Scroller;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class Sidebar {

    protected transient final BukkitCore bukkit = BukkitCore.getInstance();

    private final Player owner;

    private final Scoreboard scoreboard;
    private final Objective objective;

    private String title;
    private Scroller animation;

    private final List<SidebarRow> rows;

    private int rowIndex, rowCount;
    @Setter
    private boolean showing, isAnimated = true;

    public Sidebar(Player owner, String title) {
        this.owner = owner;

        // Usar o scoreboard existente do jogador ao invés de criar um novo
        // Isso preserva os teams criados pelo TagManager
        Scoreboard existingScoreboard = owner.getScoreboard();
        if (existingScoreboard == null || existingScoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            owner.setScoreboard(this.scoreboard);
        } else {
            this.scoreboard = existingScoreboard;
        }

        Objective objective = scoreboard.getObjective(bukkit.getName().toLowerCase());

        if (objective == null)
            objective = scoreboard.registerNewObjective(bukkit.getName().toLowerCase(), "dummy");

        this.objective = objective;

        this.title = title;

        this.rows = new CopyOnWriteArrayList<>();

        this.rowIndex = 0;
        this.rowCount = 0;

        // Objective Settings
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(Util.color(title.length() > 32 ? title.substring(0, 32) : title));

        owner.setScoreboard(scoreboard);
    }

    public void display() {
        rowIndex = 0;

        for (SidebarRow row : rows) {
            if (row == null) continue;

            String lineText = Util.color(row.getPrefix() + row.getSuffix());
            if (lineText.length() > 40) {
                lineText = lineText.substring(0, 40); // Truncate to fit within maximum scoreboard length
            }

            int slot = rows.size() - rowIndex;

            Score score = objective.getScore(lineText);
            score.setScore(slot);

            row.setSlot(slot);
            row.setScore(score);
            rowIndex += 1;
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.showing = true;

        if (isAnimated) {
            this.animation = new Scroller(ChatColor.stripColor(title).toUpperCase(), "§6§l", "§f§l", "§e§l");
        }

        BukkitCore.getManager().getSidebar().save(this);
    }

    public void clear() {
        // Remover apenas os scores das linhas da sidebar, não todas as entries do scoreboard
        // Isso preserva os teams criados pelo TagManager para o tab
        for (SidebarRow row : rows) {
            if (row != null) {
                String lineText = Util.color(row.getPrefix() + row.getSuffix());
                if (lineText != null && !lineText.isEmpty()) {
                    scoreboard.resetScores(lineText);
                }
            }
        }
        rows.clear();
        rowIndex = 0;
        rowCount = 0;
    }

    public void reset() {
        clear();
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }

    public void hide() {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        // Remover apenas os scores das linhas da sidebar, não todas as entries
        for (SidebarRow row : rows) {
            if (row != null && row.getPrefix() != null && !row.getPrefix().isEmpty()) {
                scoreboard.resetScores(row.getPrefix());
            }
        }
        showing = false;
        rowIndex = 0;
    }

    public void setTitle(String title) {
        this.title = title;
        objective.setDisplayName(title);
    }

    public boolean hasRow(String field) {
        return rows.stream().anyMatch(row -> row.getField().equalsIgnoreCase(field));
    }

    public SidebarRow getRow(String field) {
        return rows.stream().filter(row -> row.getField().equalsIgnoreCase(field)).findFirst().orElse(null);
    }

    public void addRow(String field, String text) {
        if (!hasRow(field)) {
            String truncatedText = text.length() > 40 ? text.substring(0, 40) : text; // Truncate if necessary
            SidebarRow row = new SidebarRow(field, truncatedText);
            rows.add(row);
            rowCount++;
        }
    }

    public void updateRow(SidebarRow row) {
        updateRow(row.getField(), row.getPrefix());
    }

    public void updateRow(String field, String text) {
        if (hasRow(field)) {
            SidebarRow row = getRow(field);

            if (row != null) {
                String truncatedText = text.length() > 40 ? text.substring(0, 40) : text; // Truncate if necessary

                // Verifique se o texto realmente mudou antes de atualizar a linha
                if (!truncatedText.equals(row.getPrefix())) {
                    // Remover apenas o score antigo, não resetar todo o scoreboard
                    String oldText = row.getPrefix();
                    if (oldText != null && !oldText.isEmpty()) {
                        scoreboard.resetScores(oldText);
                    }

                    row.setPrefix(truncatedText);
                    row.setSuffix("");

                    Score score = objective.getScore(truncatedText);
                    score.setScore(row.getSlot());

                    row.setScore(score);
                }
            }
        }
    }

    public void blankRow() {
        addRow(randomColor(rowCount), randomColor(rowCount));
    }

    public void addWebsiteRow(String color) {
        addRow("website", color + Constant.SERVER_DOMAIN);
    }

    public void addWebsiteRow() {
        addWebsiteRow("&e");
    }

    protected String randomColor(int row) {
        return ChatColor.values()[row].toString() + ChatColor.RESET;
    }

    public Player getOwner() {
        return owner;
    }
}
