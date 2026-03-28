package com.minecraft.savagemc.arcade.aquatic.arcade.item;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemList {

    private final static List<Item> itemList = new ArrayList<>();

    static {
        itemList.addAll(Arrays.asList(
                Item.of(Material.ENDER_PEARL).amount(2),
                Item.of(Material.ENDER_PEARL).amount(3),
                Item.of(Material.GOLDEN_APPLE).amount(2),
                Item.of(Material.GOLDEN_APPLE).amount(4),
                Item.of(Material.FIREBALL, "§cBola de Fogo").amount(1),
                Item.of(Material.MAGMA_CREAM, "§eEstilingue").amount(1),
                Item.of(Material.POTION, 8201, "§cPoção de Força I (15s)").flags(ItemFlag.values()),
                Item.of(Material.WOOD_SWORD)
                        .enchantment(Enchantment.FIRE_ASPECT, 1)
                        .durability(Material.WOOD_SWORD.getMaxDurability() - 3)
        ));
    }

    public static Item getRandomItem() {
        return itemList.get(Savage.RANDOM.nextInt(itemList.size()));
    }
}
