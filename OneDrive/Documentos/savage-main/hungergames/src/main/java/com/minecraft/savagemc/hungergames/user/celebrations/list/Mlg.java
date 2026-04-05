package com.minecraft.savagemc.hungergames.user.celebrations.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.celebrations.Celebration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Mlg extends Celebration {

    public Mlg(HungerGames hungerGames) {
        super(hungerGames);
        setDisplayName("Balde de Mlg");
        setDescription("Receba um balde de água para fazer o Mlg final!");
        setIcon(Pattern.of(Material.WATER_BUCKET));
        setFree(true);
    }

    @Override
    public void onVictory(Player player) {
        player.getInventory().setItem(0, new ItemFactory(Material.WATER_BUCKET).setName("§aMLG!").setDescription("§7Vitória de " + player.getName()).getStack());
    }

}
