package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Forger extends Kit {

    public Forger(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.COAL));
        setItems(new ItemFactory(Material.COAL).setName("§aCoal").setDescription("§7Kit Forger").setAmount(3).getStack());
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && currentItem.getType() != Material.AIR &&
                isUser((Player) event.getWhoClicked())) {
            int coalAmount = 0;
            Inventory inv = event.getView().getBottomInventory();
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() == Material.COAL)
                    coalAmount += item.getAmount();
            }
            if (coalAmount == 0)
                return;
            int hadCoal = coalAmount;
            if (currentItem.getType() == Material.COAL) {
                for (int slot = 0; slot < inv.getSize(); slot++) {
                    ItemStack item = inv.getItem(slot);
                    if (item != null && item.getType().name().contains("ORE")) {
                        coalAmount = getCoalAmount(event, coalAmount, item);
                        if (item.getAmount() == 0)
                            inv.setItem(slot, new ItemStack(0));
                    }
                }
            } else if (currentItem.getType().name().contains("ORE")) {
                coalAmount = getCoalAmount(event, coalAmount, currentItem);
                if (currentItem.getAmount() == 0)
                    event.setCurrentItem(new ItemStack(0));
            }
            if (coalAmount != hadCoal)
                for (int slot = 0; slot < inv.getSize(); slot++) {
                    ItemStack item = inv.getItem(slot);
                    if (item != null && item.getType() == Material.COAL) {
                        while (coalAmount < hadCoal && item.getAmount() > 0) {
                            item.setAmount(item.getAmount() - 1);
                            coalAmount++;
                        }
                        if (item.getAmount() == 0)
                            inv.setItem(slot, new ItemStack(0));
                    }
                }
        }
    }

    protected int getCoalAmount(InventoryClickEvent event, int coalAmount, ItemStack item) {
        while (item.getAmount() > 0 && coalAmount > 0 && (item.getType() == Material.IRON_ORE || item.getType() == Material.GOLD_ORE)) {
            item.setAmount(item.getAmount() - 1);
            coalAmount--;
            if (item.getType() == Material.IRON_ORE) {
                event.getWhoClicked().getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1));
                continue;
            }
            if (item.getType() == Material.GOLD_ORE)
                event.getWhoClicked().getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
        }
        return coalAmount;
    }

}
