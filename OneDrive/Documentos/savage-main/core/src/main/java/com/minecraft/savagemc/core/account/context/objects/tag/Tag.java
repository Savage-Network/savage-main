package com.minecraft.savagemc.core.account.context.objects.tag;

import com.minecraft.savagemc.core.account.context.objects.rank.type.RankType;
import com.minecraft.savagemc.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.savagemc.core.account.context.objects.tag.role.TagRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum Tag {

    MEMBER("Membro", "Z", "§7", emptyList(), RankType.MEMBER, TagRole.NONE, ChatColor.GRAY),

    /* Custom Role Tags */
    RED("Vermelho",   "§c§lV §r§c", "A", TagRole.COLORED, ChatColor.RED),
    BLUE("Azul",      "§9§lA §r§9", "B", TagRole.COLORED, ChatColor.BLUE),
    PINK("Rosa",      "§d§lR §r§d", "C", TagRole.COLORED, ChatColor.LIGHT_PURPLE),
    ORANGE("Laranja", "§6§lL §r§6", "D", TagRole.COLORED, ChatColor.GOLD),
    YELLOW("Amarelo", "§e§lA §r§e", "E", TagRole.COLORED, ChatColor.YELLOW),
    GREEN("Verde",    "§a§lV §r§a", "F", TagRole.COLORED, ChatColor.GREEN),
    AQUA("Ciano",     "§b§lC §r§b", "G", TagRole.COLORED, ChatColor.AQUA),
    GRAY("Cinza",     "§8§lC §r§8", "H", TagRole.COLORED, ChatColor.DARK_GRAY),
    MASKED("Camuflada", "X", "§7§k", TagRole.COLORED, ChatColor.MAGIC),
    SPECTATOR("Espectador", "X", "§7", emptyList(), RankType.MEMBER, TagRole.COLORED, ChatColor.GRAY),

    /* Special */
    TAG_2026("2026", "§b§l2026 §r§b", TagRole.SPECIAL, ChatColor.AQUA),
    GOAT("Goat",     "§e§lGOAT §r§e", TagRole.SPECIAL, ChatColor.YELLOW),

    /* VIP */
    VIP("VIP",           "N", "§a§lVIP §r§a",         emptyList(), RankType.VIP,      TagRole.VIP, ChatColor.GREEN),
    NATURE("Nature",     "M", "§e§lNATURE §r§e",       emptyList(), RankType.NATURE,   TagRole.VIP, ChatColor.YELLOW),
    EMBER("Ember",       "L", "§c§lEMBER §r§c",        emptyList(), RankType.EMBER,    TagRole.VIP, ChatColor.RED),
    SAVAGE("Savage",     "K", "§2§lSAVAGE §r§2",       emptyList(), RankType.SAVAGE,   TagRole.VIP, ChatColor.DARK_GREEN),
    CHAMPION("Champion", "J", "§6§lCHAMPION §r§6",     emptyList(), RankType.CHAMPION, TagRole.VIP, ChatColor.GOLD),
    BETA("Beta",         "I", "§1§lBETA §r§1",         emptyList(), RankType.BETA,     TagRole.VIP, ChatColor.DARK_BLUE),

    /* Mídia / Creator */
    YOUTUBER("Youtuber",   "H2", "§b§lYOUTUBER §r§b",  emptyList(), RankType.YOUTUBER,  TagRole.VIP, ChatColor.AQUA),
    STREAMER("Streamer",   "H3", "§b§lSTREAMER §r§b",  emptyList(), RankType.STREAMER,  TagRole.VIP, ChatColor.AQUA),
    TIKTOKER("Tiktoker",   "H4", "§b§lTIKTOKER §r§b",  emptyList(), RankType.TIKTOKER,  TagRole.VIP, ChatColor.AQUA),
    MIDIA("Mídia",         "H5", "§b§lMÍDIA §r§b",     emptyList(), RankType.MIDIA,     TagRole.VIP, ChatColor.AQUA),
    MIDIA_PLUS("Mídia+",   "H6", "§3§lMÍDIA+ §r§3",    singletonList("midia+"), RankType.MIDIA_PLUS, TagRole.VIP, ChatColor.DARK_AQUA),

    /* Staff */
    BUILDER("Builder",   "G", "§2§lBUILDER §r§2",   emptyList(), RankType.BUILDER,  TagRole.STAFF, ChatColor.DARK_GREEN),
    STUDIO("Studio",     "F", "§2§lSTUDIO §r§2",    emptyList(), RankType.STUDIO,   TagRole.STAFF, ChatColor.DARK_GREEN),
    HELPER("Helper",     "E", "§9§lHELPER §r§9",    emptyList(), RankType.HELPER,   TagRole.STAFF, ChatColor.BLUE),
    TRIAL("Trial",       "D", "§5§lTRIAL §r§5",     emptyList(), RankType.TRIAL,    TagRole.STAFF, ChatColor.DARK_PURPLE),
    MOD("Mod",           "C", "§5§lMOD §r§5",       emptyList(), RankType.MOD,      TagRole.STAFF, ChatColor.DARK_PURPLE),
    MODPLUS("Mod+",      "B", "§5§lMOD§5+ §r§5",    emptyList(), RankType.MODPLUS,  TagRole.STAFF, ChatColor.DARK_PURPLE),
    ADMIN("Admin",       "A", "§4§lADMIN §r§4",     emptyList(), RankType.ADMIN,    TagRole.STAFF, ChatColor.DARK_RED),
    CHEFE("Chefe",       "AA","§4§lCHEFE §r§4",     emptyList(), RankType.CHIEF,    TagRole.STAFF, ChatColor.DARK_RED);

    private final String name, order, prefix;
    private final List<String> surnames;
    private final RankType source;
    private final TagRole role;
    private final ChatColor color;
    private final List<UUID> onlyThese;

    Tag(String name, String order, String prefix, List<String> surnames, RankType source, TagRole role, ChatColor color) {
        this(name, order, prefix, surnames, source, role, color, new ArrayList<>());
    }

    Tag(String name, String prefix, TagRole role, ChatColor color) {
        this(name, prefix, "Y", role, color);
    }

    Tag(String name, String prefix, String order, TagRole role, ChatColor color) {
        this(name, order, prefix, emptyList(), RankType.MEMBER, role, color, new ArrayList<>());
    }

    public static Tag of(String name) {
        return Arrays.stream(values())
                .filter(tag -> tag.name().equalsIgnoreCase(name)
                        || tag.getName().equalsIgnoreCase(name)
                        || tag.getSurnames().stream().anyMatch(s -> s.equalsIgnoreCase(name)))
                .findFirst()
                .orElse(null);
    }

    public static Tag of(RankType source) {
        if (source.equals(RankType.CHIEF)) return CHEFE;

        return Arrays.stream(values())
                .filter(tag -> tag.getSource().equals(source))
                .findFirst()
                .orElse(null);
    }

    public String getByPrefix(TagPrefix prefix) {
        if (this == SPECTATOR || this == MEMBER) return getPrefix();

        String prefixRegex = getPrefix().split(" ")[0];
        String result = prefixRegex;

        switch (prefix) {
            case DEFAULT:            result = getPrefix(); break;
            case COLOR:              result = getColor().toString(); break;
            case BRACKETS: {
                String n = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName().toUpperCase();
                result = getColor() + "[" + n + "] ";
                break;
            }
            case BRACKETS_NORMAL_CASE: {
                String n = isRole(TagRole.COLORED)
                        ? ChatColor.stripColor(getPrefix()).split(" ")[0]
                        : getName();
                result = getColor() + "[" + n + "] ";
                break;
            }
            case DEFAULT_WHITE: result = prefixRegex + " §r"; break;
            case DEFAULT_GRAY:  result = prefixRegex + " §r§7"; break;
        }

        return result;
    }

    public boolean isOnlyThese(UUID id) {
        return !onlyThese.isEmpty() && onlyThese.contains(id);
    }

    public boolean isRole(TagRole role) {
        return this.role.equals(role);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getPermission() {
        return "tag." + name.toLowerCase();
    }
}
