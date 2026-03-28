package com.minecraft.savagemc.arcade.aquatic.perk.list;

import com.minecraft.savagemc.arcade.aquatic.perk.Perk;
import com.minecraft.savagemc.core.api.item.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public class Repulsor extends Perk {

    public Repulsor() {
        super("Repulsor", Item.of(Material.STICK)
                .enchantment(Enchantment.KNOCKBACK, 1)
                .flags(ItemFlag.values()));

        setLore("§7Jogue os seus adversários", "§7para longe.");

        setItems(Item.of(Material.STICK, "§aRepulsor")
                .enchantment(Enchantment.KNOCKBACK, 1)
                .flags(ItemFlag.values())
                .lore("§7Repulsão I"));
    }
}
