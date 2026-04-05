package com.minecraft.savagemc.hungergames.user.celebrations;

import com.minecraft.core.bukkit.util.listener.DynamicListener;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.core.enums.Rank;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.celebrations.pattern.CelebrationRarity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public abstract class Celebration extends DynamicListener {

    private final HungerGames hungerGames; // Hunger Games instance

    private final String name;
    private Rank rank;

    private Pattern icon;
    private String displayName, description;
    private CelebrationRarity rarity;
    public boolean isFree;

    public Celebration(final HungerGames hungerGames) {
        this.hungerGames = hungerGames;
        this.rarity = CelebrationRarity.COMMON;
        this.rank = Rank.ADMINISTRATOR;
        this.displayName = getClass().getSimpleName();
        this.name = getClass().getSimpleName();
        this.isFree = false;
    }


    public abstract void onVictory(Player player);

}
