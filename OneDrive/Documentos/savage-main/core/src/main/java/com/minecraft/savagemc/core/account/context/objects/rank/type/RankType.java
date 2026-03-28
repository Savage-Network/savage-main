package com.minecraft.savagemc.core.account.context.objects.rank.type;

import com.minecraft.savagemc.core.account.context.objects.rank.type.role.RankRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum RankType {

    /* None */
    MEMBER("Membro", RankRole.NONE, ChatColor.GRAY, emptyList()),

    /* VIP */
    VIP("VIP",         RankRole.VIP, ChatColor.GREEN,        emptyList()),
    NATURE("Nature",   RankRole.VIP, ChatColor.YELLOW,       emptyList()),
    EMBER("Ember",     RankRole.VIP, ChatColor.RED,          emptyList()),
    SAVAGE("Savage",   RankRole.VIP, ChatColor.DARK_GREEN,   emptyList()),
    CHAMPION("Champion", RankRole.VIP, ChatColor.GOLD,       emptyList()),
    BETA("Beta",       RankRole.VIP, ChatColor.DARK_BLUE,    emptyList()),

    /* Creator / Mídia */
    YOUTUBER("Youtuber",   RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    STREAMER("Streamer",   RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    TIKTOKER("Tiktoker",   RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    MIDIA("Mídia",         RankRole.SPECIAL, ChatColor.AQUA, emptyList()),
    MIDIA_PLUS("Mídia+",   RankRole.SPECIAL, ChatColor.DARK_AQUA, emptyList()),

    /* Staff */
    BUILDER("Builder",     RankRole.STAFF, ChatColor.DARK_GREEN,   emptyList()),
    STUDIO("Studio",       RankRole.STAFF, ChatColor.DARK_GREEN,   emptyList()),
    HELPER("Helper",       RankRole.STAFF, ChatColor.BLUE,         emptyList()),
    TRIAL("Trial",         RankRole.STAFF, ChatColor.DARK_PURPLE,  emptyList()),
    MOD("Mod",             RankRole.STAFF, ChatColor.DARK_PURPLE,  emptyList()),
    MODPLUS("Mod+",        RankRole.STAFF, ChatColor.DARK_PURPLE,  emptyList()),
    ADMIN("Admin",         RankRole.STAFF, ChatColor.DARK_RED,     singletonList("*")),
    CHIEF("Chief",         RankRole.STAFF, ChatColor.DARK_RED,     singletonList("*"));

    private final String name;
    private final RankRole role;
    private final ChatColor color;
    private final List<String> permissions;

    public static RankType of(String name) {
        return Arrays.stream(values())
                .filter(rank -> rank.name().equalsIgnoreCase(name) || rank.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getBoldColorName() {
        return color + "§l" + name;
    }

    public RankType bellow() {
        return this == MEMBER ? MEMBER : values()[ordinal() - 1];
    }
}
