package com.minecraft.savagemc.core.account.context.objects.medal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Medal {

    NONE("Nenhuma", "", ChatColor.WHITE, null),

    TOP1("Top 1°",  "§4✪", ChatColor.DARK_RED,  "medal.top1"),
    TOP2("Top 2°",  "§b✪", ChatColor.AQUA,      "medal.top2"),
    TOP3("Top 3°",  "§3✪", ChatColor.DARK_AQUA, "medal.top3"),
    TOP4("Top 4°",  "§1✪", ChatColor.DARK_BLUE, "medal.top4"),

    ADMIN_STAR("Estrela Admin",  "§4✠", ChatColor.DARK_RED,  "medal.admin"),
    ADMIN_CROSS("Cruz Admin",    "§4✟", ChatColor.DARK_RED,  "medal.admin.cross"),
    BETA("Beta Tester",          "§3⚝", ChatColor.DARK_AQUA, "medal.beta"),
    STAFF_SHIELD("Escudo Staff", "§9✜", ChatColor.BLUE,      "medal.staff"),
    STAFF_CROSS("Cruz Staff",    "§9☩", ChatColor.BLUE,      "medal.staff.cross"),
    STAFF_WISH("Desejo Staff",   "§9✦", ChatColor.BLUE,      "medal.staff.wish"),

    STAR("Estrela",          "§e✯", ChatColor.YELLOW,       "medal.star"),
    SPARKLE("Brilho",        "§e✵", ChatColor.YELLOW,       "medal.sparkle"),
    DIAMOND("Diamante",      "§b✦", ChatColor.AQUA,         "medal.diamond"),
    CRYSTAL("Cristal",       "§b✧", ChatColor.AQUA,         "medal.crystal"),
    SUN("Sol",               "§6☀", ChatColor.GOLD,         "medal.sun"),
    MOON("Lua",              "§f☾", ChatColor.WHITE,        "medal.moon"),
    FLOWER("Flor",           "§a❀", ChatColor.GREEN,        "medal.flower"),
    ROSE("Rosa",             "§d✿", ChatColor.LIGHT_PURPLE, "medal.rose"),
    BLOOM("Florir",          "§a❁", ChatColor.GREEN,        "medal.bloom"),
    CLOVER("Trevo",          "§a❃", ChatColor.GREEN,        "medal.clover"),
    NOTE("Nota",             "§e♩", ChatColor.YELLOW,       "medal.note"),
    MUSIC("Música",          "§e♪", ChatColor.YELLOW,       "medal.music"),
    HARMONY("Harmonia",      "§e♫", ChatColor.YELLOW,       "medal.harmony"),
    MELODY("Melodia",        "§6♬", ChatColor.GOLD,         "medal.melody"),
    SNOWFLAKE("Neve",        "§b❆", ChatColor.AQUA,         "medal.snowflake"),
    HEART("Coração",         "§c❥", ChatColor.RED,          "medal.heart"),
    CHECK("Check",           "§a✓", ChatColor.GREEN,        "medal.check"),
    ARROW("Seta",            "§6➤", ChatColor.GOLD,         "medal.arrow"),
    YIN("Yin Yang",          "§f☯", ChatColor.WHITE,        "medal.yin"),
    BIOHAZARD("Perigo",      "§2☣", ChatColor.DARK_GREEN,   "medal.biohazard"),
    ASTERISK("Asterisco",    "§c✶", ChatColor.RED,          "medal.asterisk"),
    BURST("Explosão",        "§6✷", ChatColor.GOLD,         "medal.burst"),
    SPARK("Faísca",          "§e✸", ChatColor.YELLOW,       "medal.spark"),
    FLARE("Flare",           "§c✹", ChatColor.RED,          "medal.flare"),
    RING("Anel",             "§6✺", ChatColor.AQUA,         "medal.ring"),
    MAGIC_RING("Anel Mágico","§6§l℧", ChatColor.GOLD,      "medal.magic_ring"),
    GEAR("Engrenagem",       "§7✻", ChatColor.GRAY,         "medal.gear"),
    WHEEL("Roda",            "§7✼", ChatColor.GRAY,         "medal.wheel"),
    PETAL("Pétala",          "§d✽", ChatColor.LIGHT_PURPLE, "medal.petal"),
    BLOSSOM("Flor Completa", "§a✾", ChatColor.GREEN,        "medal.blossom"),
    PENCIL("Lápis",          "§f✐", ChatColor.WHITE,        "medal.pencil"),
    WISH("Desejo",           "§6✦", ChatColor.GOLD,         "medal.wish"),
    ZRAFA("zrwfa", "§4❥", ChatColor.DARK_RED, "medal.zrafa");

    private final String name, coloredSymbol;
    private final ChatColor color;
    private final String permission;

    public static Medal of(String name) {
        return Stream.of(values())
                .filter(medal -> medal.name().equalsIgnoreCase(name) || medal.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getSymbol() {
        return coloredSymbol;
    }

    public String getColoredName() {
        return color + name;
    }

    public String getColoredSymbol() {
        return coloredSymbol;
    }

    public String getPermission() {
        return permission != null ? permission : "medal." + name().toLowerCase();
    }
}
