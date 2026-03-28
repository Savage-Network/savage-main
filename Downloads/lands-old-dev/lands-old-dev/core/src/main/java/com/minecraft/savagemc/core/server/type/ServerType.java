package com.minecraft.savagemc.core.server.type;

import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.arcade.style.ArcadeStyle;
import com.minecraft.savagemc.core.server.category.ServerCategory;
import com.minecraft.savagemc.core.server.flag.ServerFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Getter
@AllArgsConstructor
public enum ServerType {

    BUNGEE("Savage", "bungee", "", ServerCategory.NONE, ArcadeStyle.NONE, false),
    AUTH("Auth", "auth", "", ServerCategory.NONE, ArcadeStyle.NONE, false),

    /* Hub Servers */
    HUB("Savage", "hub", "", ServerCategory.NONE, ArcadeStyle.NONE, false),
    HUB_DUELS("Practice", "hub-duels", "", ServerCategory.NONE, ArcadeStyle.NONE, false),
    HUB_BEDWARS("Bed Wars", "hub-bedwars", "", ServerCategory.NONE, ArcadeStyle.NONE,
            new ArrayList<>(), asList("bedwars", "bed", "bw", "bed-wars"), false),
    HUB_PVP("PvP", "hub-pvp", "", ServerCategory.NONE, ArcadeStyle.NONE, false),
    HUB_AQUATIC("Water Fight", "hub-aquatic", "", ServerCategory.NONE, ArcadeStyle.NONE,
            new ArrayList<>(), asList("aquatic", "waterfight", "water-fight"), false),
    HUB_HUNGERGAMES("Hunger Games", "lobbyhg", "", ServerCategory.NONE, ArcadeStyle.NONE,
            new ArrayList<>(), asList("lobbyhg", "hub-hg", "hub-hungergames", "lobby-hg"), false),

    /* Arcade Servers */
    DUELS("Practice", "duels", "DIAMOND_SWORD", ServerCategory.ARCADE, ArcadeStyle.COMBAT,
            asList(ServerFlag.NAME_TAG_CHANGE, ServerFlag.RANKED, ServerFlag.CUSTOM_ROOM), true),
    BEDWARS("Bed Wars", "bedwars", "BED", ServerCategory.ARCADE, ArcadeStyle.STRATEGY,
            asList(ServerFlag.NAME_TAG_CHANGE, ServerFlag.RECONNECT, ServerFlag.MULTI_SERVER, ServerFlag.RANKED, ServerFlag.CUSTOM_ROOM), true),
    PVP("PvP", "pvp", "IRON_CHESTPLATE", ServerCategory.ARCADE, ArcadeStyle.COMBAT,
            singletonList(ServerFlag.RANKED), true),
    AQUATIC("Water Fight", "aquatic", "WATER_BUCKET", ServerCategory.ARCADE, ArcadeStyle.COMBAT,
            asList(ServerFlag.NAME_TAG_CHANGE, ServerFlag.CUSTOM_ROOM), true),
    SKY_WARS("Sky Wars", "skywars", "BOW", ServerCategory.ARCADE, ArcadeStyle.COMBAT,
            asList(ServerFlag.NAME_TAG_CHANGE, ServerFlag.RANKED), true),

    /* Default Servers */
    HUNGERGAMES("lobby-hg", "hg", "MUSHROOM_SOUP", ServerCategory.NONE, ArcadeStyle.COMBAT,
            singletonList(ServerFlag.RANKED), true),
    ;

    private final String name;
    private final String id, iconId;

    private final ServerCategory category;
    private final ArcadeStyle style;

    private final List<ServerFlag> flags;
    private final List<String> aliases;

    private final boolean shown;

    ServerType(String name, String id, String iconId, ServerCategory category, ArcadeStyle style, boolean shown) {
        this(name, id, iconId, category, style, new ArrayList<>(), new ArrayList<>(), shown);
    }

    ServerType(String name, String id, String iconId, ServerCategory category, ArcadeStyle style, List<ServerFlag> flags, boolean shown) {
        this(name, id, iconId, category, style, flags, new ArrayList<>(), shown);
    }

    public static ServerType of(String name) {
        return list(type -> type.name().equalsIgnoreCase(name) || type.getName().equalsIgnoreCase(name) || type.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(name)))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static List<ServerType> list() {
        return Stream.of(values()).collect(Collectors.toList());
    }

    public static List<ServerType> list(Predicate<ServerType> filter) {
        return list().stream().filter(filter).collect(Collectors.toList());
    }

    public boolean isLobby() {
        return name().startsWith("HUB_");
    }

    public ServerType getServerLobby() {
        return list(type -> type.isLobby() && type.name.endsWith(name)).stream().findFirst().orElse(null);
    }

    public ServerType getRealServerOfLobby() {
        if (!isLobby()) return null;

        String realName = name().split("_")[1];

        return list(type -> !type.isLobby() && (type.name().equalsIgnoreCase(realName) || type.getName().equalsIgnoreCase(name)))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public ArcadeCategory getArcade() {
        return ArcadeCategory.of(name());
    }

    public boolean hasFlag(ServerFlag flag) {
        return this.flags.contains(flag);
    }

    public boolean isArcade() {
        if (this == HUB) return false;

        return category.equals(ServerCategory.ARCADE);
    }
}
