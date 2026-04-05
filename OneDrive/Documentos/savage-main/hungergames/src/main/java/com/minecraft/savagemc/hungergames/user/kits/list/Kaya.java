package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.user.User;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;

public class Kaya extends Kit {

    private final HashMap<Block, Player> kayaBlocks = new HashMap<>();

    public Kaya(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.GRASS));
        setItems(new ItemFactory(Material.GRASS).setName("§aKaya").setAmount(24).getStack());
        setKitCategory(KitCategory.STRATEGY);
        setPrice(25000);

        ShapelessRecipe recipe = new ShapelessRecipe(new ItemFactory(Material.GRASS).setName("§aKaya").getStack());

        recipe.addIngredient(Material.DIRT);
        recipe.addIngredient(Material.SEEDS);

        getPlugin().getServer().addRecipe(recipe);
    }

    @EventHandler
    public void kaya(PlayerMoveEvent event) {
        if (!User.fetch(event.getPlayer().getUniqueId()).isAlive())
            return;
        Location loc = event.getPlayer().getLocation();
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                Block block = loc.clone().add(x, -1.0D, z).getBlock();
                if (this.kayaBlocks.containsKey(block) && block.getType() == Material.GRASS && this.kayaBlocks.get(block) != event.getPlayer()) {
                    block.setType(Material.AIR);
                    this.kayaBlocks.remove(block);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        this.kayaBlocks.remove(event.getBlock());
    }

    @EventHandler
    public void kaya(EntityExplodeEvent event) {
        for (Block b : event.blockList())
            this.kayaBlocks.remove(b);
    }

    @EventHandler
    public void kaya(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks())
            this.kayaBlocks.remove(b);
    }

    @EventHandler(ignoreCancelled = true)
    public void kaya(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.GRASS && isUser(event.getPlayer()))
            this.kayaBlocks.put(event.getBlock(), event.getPlayer());
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult() != null && event.getRecipe().getResult().getType() == Material.GRASS) {
            for (HumanEntity entity : event.getViewers()) {
                if (isUser((Player) entity))
                    return;
            }
            event.getInventory().setItem(0, new ItemStack(Material.AIR));
        }
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        Player player = event.getKiller().getPlayer();
        if (!isUser(player))
            return;
        this.kayaBlocks.keySet().removeIf(b -> (this.kayaBlocks.get(b) == player));
    }

}
