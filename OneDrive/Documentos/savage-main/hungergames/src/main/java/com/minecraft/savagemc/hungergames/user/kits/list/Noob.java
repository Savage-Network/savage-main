package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Noob extends Kit {

    public Noob(HungerGames hungerGames) {
        super(hungerGames);
        setItems();
        setIcon(Pattern.of(Material.SKULL_ITEM, 4));
        setKitCategory(KitCategory.STRATEGY);
    }

    @Override
    public void grant(Player player) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD), new ItemStack(Material.STONE_AXE),
                new ItemStack(Material.STONE_HOE), new ItemStack(Material.STONE_PICKAXE), new ItemStack(Material.MUSHROOM_SOUP, 16));
    }

}
