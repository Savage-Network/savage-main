package com.minecraft.savagemc.core.member.list.aquatic;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.arcade.category.ArcadeCategory;
import com.minecraft.savagemc.core.member.Member;
import com.minecraft.savagemc.core.member.context.stats.ArcadeStats;

import java.util.UUID;

public class AquaticMember extends Member {

    public AquaticMember(UUID id, String name) {
        super(id, name);
    }

    @Override
    public void save(String... fields) {
        for (String field : fields)
            Savage.getAquaticData().update(this, field);
    }

    public int getTotalMatches() {
        return getStats().values().stream().mapToInt(ArcadeStats::getMatches).sum();
    }

    public int getTotalWins() {
        return getStats().values().stream().mapToInt(ArcadeStats::getWins).sum();
    }

    public int getTotalWins(ArcadeCategory arcade) {
        return getStats().values().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(ArcadeStats::getWins).sum();
    }

    public int getTotalWinstreak(ArcadeCategory arcade) {
        return getStats().values().stream().filter(stats -> stats.getArcade().equals(arcade)).mapToInt(ArcadeStats::getWinStreak).sum();
    }

    public int getTotalDefeats() {
        return getStats().values().stream().mapToInt(ArcadeStats::getDefeats).sum();
    }

    public int getTotalAssists() {
        return getStats().values().stream().mapToInt(ArcadeStats::getAssists).sum();
    }

    public int getTotalKills() {
        return getStats().values().stream().mapToInt(ArcadeStats::getKills).sum();
    }

    public int getTotalWinstreak() {
        return getStats().values().stream().mapToInt(ArcadeStats::getWinStreak).sum();
    }

    public int getTotalHighWinstreak() {
        return getStats().values().stream().mapToInt(ArcadeStats::getHighWinStreak).sum();
    }
}
