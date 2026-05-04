package com.minecraft.arcade.eventos.kits;

import com.minecraft.arcade.eventos.managers.VariablesManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Kits opcionais ao dar o §aGO§r (variável {@code event.kit}: none | pvp | knockback).
 */
public final class EventKitApplier {

    private EventKitApplier() {
    }

    public static void aplicarAoInicioDeJogo(Player p, VariablesManager vars) {
        if (p == null || !p.isOnline()) return;
        String kit = normalize(vars.get("event.kit"));
        switch (kit) {
            case "pvp":
                p.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
                p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
                p.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                p.updateInventory();
                break;
            case "knockback":
                ItemStack bastao = new ItemStack(Material.STICK, 1);
                bastao.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
                p.getInventory().addItem(bastao);
                p.updateInventory();
                break;
            case "none":
            default:
                break;
        }
    }

    private static String normalize(String s) {
        if (s == null) return "none";
        return s.trim().toLowerCase();
    }
}
