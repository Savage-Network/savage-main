package com.minecraft.savagemc.core.account.context.objects.clantag;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
public enum ClanTag {

    NONE("Nenhuma", "", ChatColor.WHITE, RankType.MEMBER, null),

    ADMIN("Admin",        "§4",  ChatColor.DARK_RED,    RankType.ADMIN,  "clantag.admin"),
    CHAMPION("Champion",  "§6",  ChatColor.GOLD,        RankType.MEMBER, "clantag.champion"),
    PARTNER_PLUS("Partner+", "§3", ChatColor.DARK_AQUA, RankType.MEMBER, "clantag.partner_plus"),
    MOD("Mod",            "§5",  ChatColor.DARK_PURPLE, RankType.TRIAL,  "clantag.mod"),

    /* Admin-only */
    CXC("CXC",            "§c",  ChatColor.RED,         RankType.ADMIN,  "clantag.cxc"),
    SAVAGE("Savage",      null,  ChatColor.DARK_GREEN,  RankType.ADMIN,  "clantag.savage");

    private static final String[] SAVAGE_COLORS = {"§2", "§2", "§2", "§a", "§a", "§a"};

    private final String name, colorCode;
    private final ChatColor color;
    private final RankType requiredRank;
    private final String permission;

    ClanTag(String name, String colorCode, ChatColor color, RankType requiredRank, String permission) {
        this.name = name;
        this.colorCode = colorCode;
        this.color = color;
        this.requiredRank = requiredRank;
        this.permission = permission;
    }

    public static ClanTag of(String name) {
        return Arrays.stream(values())
                .filter(ct -> ct.name().equalsIgnoreCase(name) || ct.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getColoredName() {
        return color + name;
    }

    public String format(String clanTag) {
        if (this == SAVAGE) return colorize(clanTag, SAVAGE_COLORS);
        return colorCode != null ? colorCode + clanTag : clanTag;
    }

    private static String colorize(String text, String[] colors) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (char c : text.toCharArray()) {
            sb.append(colors[i % colors.length]).append(c);
            i++;
        }
        return sb.toString();
    }
}
