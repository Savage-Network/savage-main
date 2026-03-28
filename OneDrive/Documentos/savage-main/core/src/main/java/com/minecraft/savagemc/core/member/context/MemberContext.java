package com.minecraft.savagemc.core.member.context;

import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.member.context.elo.Elo;
import com.minecraft.savagemc.core.member.context.menu.MenuMetadata;
import com.minecraft.savagemc.core.member.context.stats.ArcadeStats;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class MemberContext {

    private final List<MenuMetadata> menus = new ArrayList<>();
    private final Map<ArcadeCategory, ArcadeStats> stats = new ConcurrentHashMap<>();

    private Elo elo = Elo.NONE;

    private int level, eloXp, levelXp, coins;

    private int weekEloXp, weekLevelXp, monthEloXp, monthLevelXp;

    private final long joinedAt = System.currentTimeMillis();
}
