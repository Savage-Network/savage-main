package com.minecraft.savagemc.hungergames.util.templatekit.scrim;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum KitType {

    MUSHROOM() {
        @Override
        public void apply(Player player) {

            PlayerInventory inventory = player.getInventory();

            inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
            inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            inventory.setBoots(new ItemStack(Material.IRON_BOOTS));

            inventory.setItem(0, new ItemFactory(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack());
            inventory.setItem(1, new ItemStack(Material.WATER_BUCKET));
            inventory.setItem(2, new ItemStack(Material.LAVA_BUCKET));

            inventory.setItem(8, new ItemFactory(Material.COMPASS).setDescription("§7Encontrar jogadores").setName("§aBússola").getStack());

            inventory.setItem(9, new ItemStack(Material.IRON_HELMET));
            inventory.setItem(10, new ItemStack(Material.IRON_CHESTPLATE));
            inventory.setItem(11, new ItemStack(Material.IRON_LEGGINGS));
            inventory.setItem(12, new ItemStack(Material.IRON_BOOTS));

            inventory.setItem(13, new ItemStack(Material.BOWL, 64));
            inventory.setItem(22, new ItemStack(Material.BOWL, 64));

            inventory.setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
            inventory.setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));
            inventory.setItem(16, new ItemStack(Material.RED_MUSHROOM, 64));
            inventory.setItem(17, new ItemStack(Material.BROWN_MUSHROOM, 64));
            inventory.setItem(23, new ItemStack(Material.RED_MUSHROOM, 64));
            inventory.setItem(24, new ItemStack(Material.BROWN_MUSHROOM, 64));
            inventory.setItem(25, new ItemStack(Material.RED_MUSHROOM, 64));
            inventory.setItem(26, new ItemStack(Material.BROWN_MUSHROOM, 64));

            inventory.setItem(28, new ItemStack(Material.LAVA_BUCKET));
            inventory.setItem(33, new ItemStack(Material.STONE_PICKAXE));
            inventory.setItem(34, new ItemStack(Material.STONE_AXE));
            inventory.setItem(35, new ItemStack(Material.LOG, 32));

            inventory.setItem(18, new ItemStack(Material.IRON_HELMET));
            inventory.setItem(19, new ItemStack(Material.IRON_CHESTPLATE));
            inventory.setItem(20, new ItemStack(Material.IRON_LEGGINGS));
            inventory.setItem(21, new ItemStack(Material.IRON_BOOTS));

            inventory.setItem(27, new ItemStack(Material.DIAMOND_SWORD));

        }
    },

    COCOA() {
        @Override
        public void apply(Player player) {

            PlayerInventory inventory = player.getInventory();

            inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
            inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            inventory.setBoots(new ItemStack(Material.IRON_BOOTS));

            inventory.setItem(0, new ItemFactory(Material.DIAMOND_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).getStack());
            inventory.setItem(1, new ItemStack(Material.WATER_BUCKET));
            inventory.setItem(2, new ItemStack(Material.LAVA_BUCKET));

            inventory.setItem(8, new ItemFactory(Material.COMPASS).setDescription("§7Encontrar jogadores").setName("§aBússola").getStack());

            inventory.setItem(9, new ItemStack(Material.IRON_HELMET));
            inventory.setItem(10, new ItemStack(Material.IRON_CHESTPLATE));
            inventory.setItem(11, new ItemStack(Material.IRON_LEGGINGS));
            inventory.setItem(12, new ItemStack(Material.IRON_BOOTS));

            inventory.setItem(13, new ItemStack(Material.BOWL, 64));
            inventory.setItem(22, new ItemStack(Material.BOWL, 64));

            inventory.setItem(14, new ItemStack(Material.INK_SACK, 64, (short) 3));
            inventory.setItem(15, new ItemStack(Material.INK_SACK, 64, (short) 3));
            inventory.setItem(23, new ItemStack(Material.INK_SACK, 64, (short) 3));
            inventory.setItem(24, new ItemStack(Material.INK_SACK, 64, (short) 3));

            inventory.setItem(16, new ItemStack(Material.LAVA_BUCKET));
            inventory.setItem(17, new ItemStack(Material.STONE_AXE));
            inventory.setItem(25, new ItemStack(Material.LOG, 32));
            inventory.setItem(26, new ItemStack(Material.STONE_PICKAXE));

            inventory.setItem(18, new ItemStack(Material.IRON_HELMET));
            inventory.setItem(19, new ItemStack(Material.IRON_CHESTPLATE));
            inventory.setItem(20, new ItemStack(Material.IRON_LEGGINGS));
            inventory.setItem(21, new ItemStack(Material.IRON_BOOTS));

            inventory.setItem(27, new ItemStack(Material.DIAMOND_SWORD));
        }
    };

    public abstract void apply(Player player);

    @Getter
    private static final KitType[] values;

    static {
        values = values();
    }

    public static KitType parse(String s) {
        return Arrays.stream(getValues()).filter(c -> c.name().equalsIgnoreCase(s)).findFirst().orElse(null);
    }
}
