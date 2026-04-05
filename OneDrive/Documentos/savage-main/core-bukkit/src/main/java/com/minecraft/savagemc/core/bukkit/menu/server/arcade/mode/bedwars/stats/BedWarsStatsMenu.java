package com.minecraft.savagemc.core.bukkit.menu.server.arcade.mode.bedwars.stats;

import com.minecraft.savagemc.core.Constant;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.account.Account;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.room.type.Type;
import com.minecraft.savagemc.core.bukkit.api.menu.Menu;
import com.minecraft.savagemc.core.bukkit.api.menu.sound.MenuSound;
import com.minecraft.savagemc.core.member.list.bedwars.BedMember;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.profile.ProfileType;
import com.minecraft.savagemc.core.member.list.bedwars.objects.metadata.objects.stats.BedStats;
import com.minecraft.savagemc.core.server.type.ServerType;
import com.minecraft.savagemc.core.util.Util;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BedWarsStatsMenu extends Menu {

    private final BedMember member;

    @Setter
    private Type type = Type.CASUAL;

    public BedWarsStatsMenu(Player player, Menu last) {
        super(player, "Estatísticas: Bed Wars", last, 6);

        Account account = Savage.getAccountData().of(player.getUniqueId());

        this.member = (BedMember) account.loadMember(ServerType.BEDWARS);
    }

    // Método adicional para compatibilidade
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void handle() {
        clear();

        List<String> totalLore = new ArrayList<>(Arrays.asList(
                "§7" + (type.equals(Type.CASUAL) ? "Seu nível: " + member.getLevelId() : "Rank: " + member.getElo().getFullName()),
                "§7XP: §a" + Util.formatNumber(member.isProfile(ProfileType.NORMAL) ? member.getLevelXp() : member.getEloXp()),
                ""
        ));

        totalLore.addAll(getInfoLore(member.getTotalMatches(), member.getTotalBedDestruction(), member.getTotalFinalKills(),
                member.getTotalKills(), member.getTotalAssists(), member.getTotalWins(), member.getTotalDefeats(), member.getTotalWinstreak(),
                member.getTotalHighWinstreak()));

        addItem(4, Item.of(Material.PAPER, "§aGeral", totalLore));

        int normalSlot = 10, versusSlot = 28;
        for (ArcadeCategory arcade : ArcadeCategory.of(ServerType.BEDWARS)) {
            int id = arcade.getSlots().get(0).ordinal();

            if (!arcade.name().contains("VERSUS")) {
                BedStats stats = member.getStats(arcade, type);

                addItem(normalSlot, Item.of(Material.BED, "§a" + arcade.getName(), getInfoLore(stats))
                        .amount(id));

                normalSlot += 2;
            } else {
                BedStats stats = member.getStats(arcade, Type.CASUAL);

                addItem(versusSlot, Item.of(Material.BED, "§a" + arcade.getName(), getInfoLore(stats))
                        .amount(id));

                versusSlot += 2;
            }
        }

        List<String> filterLore = new ArrayList<>(Collections.singletonList(""));

        for (Type type : Type.values()) {
            if (type.equals(Type.CUSTOM)) continue;

            filterLore.add((this.type.equals(type) ? "§a" + Constant.ARROW_ALT_SYMBOL + " " : "§7") + type.getName());
        }

        filterLore.addAll(Arrays.asList(
                "",
                "§eClique para alternar!"
        ));

        addItem(getTotalSlots() - 4, Item.of(Material.BREWING_STAND_ITEM, "§bFiltrando por:", filterLore)
                .click(event -> {
                    setType(type.equals(Type.CASUAL) ? Type.COMPETITIVE : Type.CASUAL);

                    sound(MenuSound.PAGINATED);
                    handle();
                }));

        if (isReturnable())
            addBackButton();

        display();
    }

    protected List<String> getInfoLore(BedStats stats) {
        return getInfoLore(stats.getMatches(), stats.getBedDestruction(), stats.getFinalKills(),
                stats.getKills(), stats.getAssists(), stats.getWins(), stats.getDefeats(),
                stats.getWinStreak(), stats.getHighWinStreak());
    }

    protected List<String> getInfoLore(int matches, int bedDestruction, int finalKills, int kills, int assists, int wins, int defeats, int winStreak, int highWinStreak) {
        return Arrays.asList(
                "§7Partidas jogadas: §a" + Util.formatNumber(matches),
                "",
                "§7Camas quebradas: §a" + Util.formatNumber(bedDestruction),
                "§7Kills finais: §a" + Util.formatNumber(finalKills),
                "",
                "§7Kills: §a" + Util.formatNumber(kills),
                "§7Assistências: §a" + Util.formatNumber(assists),
                "",
                "§7Vitórias: §a" + Util.formatNumber(wins),
                "§7Derrotas: §a" + Util.formatNumber(defeats),
                "",
                "§7Winstreak: §a" + Util.formatNumber(winStreak),
                "§7Maior Winstreak: §a" + Util.formatNumber(highWinStreak)
        );
    }
}
