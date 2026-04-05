package com.minecraft.savagemc.hungergames.util.item;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class ItemChance {

	private ItemStack item;
	private int chance;

	public ItemChance(ItemStack item, int chance) {
		setItem(item);
		setChance(chance);
	}

}
