package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import net.minecraft.server.v1_8_R3.InventorySubcontainer;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Backpacker extends Kit {

    protected final ImmutableSet<Action> actionImmutableSet = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    public Backpacker(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.LEATHER));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.LEATHER).setName("§aMochila").setDescription("§7Kit Backpacker").getStack());
        setPrice(20000);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && actionImmutableSet.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            final Player player = event.getPlayer();

            InventorySubcontainer chest = (InventorySubcontainer) ((CraftInventory) player.getEnderChest()).getInventory();
            chest.a("Backpack");
            player.openInventory(player.getEnderChest());
        }
    }

}
