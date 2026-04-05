package com.minecraft.savagemc.hungergames.user.kits.list;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.event.user.LivingUserDieEvent;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.MerchantRecipe;
import net.minecraft.server.v1_8_R3.MerchantRecipeList;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class Trader extends Kit {

    protected final MerchantRecipeList merchantRecipes = new MerchantRecipeList();
    protected final ImmutableSet<Action> actionImmutableSet = Sets.immutableEnumSet(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    public Trader(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.EMERALD));
        setKitCategory(KitCategory.STRATEGY);
        setItems(new ItemFactory(Material.EMERALD).setName("§aTrader").setDescription("§7Kit Trader").getStack());
        setPrice(30000);

        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 5), new ItemStack(Material.FLINT_AND_STEEL)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 5), new ItemStack(Material.ENDER_PEARL)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 5), new ItemStack(Material.ARROW)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 5), new ItemStack(Material.WOOD, 16)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 10), new ItemStack(Material.INK_SACK, 10, (short) 3)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 10), new ItemStack(Material.BOW)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 15), new ItemStack(Material.BUCKET)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 15), new ItemStack(Material.EXP_BOTTLE, 2)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 20), new ItemStack(Material.IRON_PICKAXE)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 20), new ItemStack(Material.IRON_AXE)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 20), new ItemStack(Material.GOLDEN_APPLE)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 40), new ItemStack(Material.IRON_SWORD)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 45), new ItemStack(Material.FISHING_ROD)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 50), new ItemStack(Material.IRON_LEGGINGS)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 64), new ItemStack(Material.IRON_CHESTPLATE)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 64), new ItemStack(Material.EMERALD, 36), new ItemStack(Material.ANVIL)));
        merchantRecipes.add(createMerchantRecipe(new ItemStack(Material.EMERALD, 64), new ItemStack(Material.EMERALD, 64), new ItemStack(Material.DIAMOND_SWORD)));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && actionImmutableSet.contains(event.getAction()) && isUser(event.getPlayer()) && isItem(event.getItem())) {
            event.setCancelled(true);

            final Player player = event.getPlayer();

            try {
                EntityVillager villager = new EntityVillager(((CraftPlayer) player).getHandle().world, 0);
                villager.setCustomName("Kit Trader");

                Field careerLevelField = EntityVillager.class.getDeclaredField("by");
                careerLevelField.setAccessible(true);
                careerLevelField.set(villager, 10);

                Field recipeListField = EntityVillager.class.getDeclaredField("br");
                recipeListField.setAccessible(true);
                recipeListField.set(villager, this.merchantRecipes);

                villager.a_(((CraftPlayer) player).getHandle());

                ((CraftPlayer) player).getHandle().openTrade(villager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected MerchantRecipe createMerchantRecipe(org.bukkit.inventory.ItemStack item1, org.bukkit.inventory.ItemStack item2, org.bukkit.inventory.ItemStack item3) {
        MerchantRecipe recipe = new MerchantRecipe(CraftItemStack.asNMSCopy(item1), CraftItemStack.asNMSCopy(item2), CraftItemStack.asNMSCopy(item3));
        try {
            Field maxUsesField = MerchantRecipe.class.getDeclaredField("maxUses");
            maxUsesField.setAccessible(true);
            maxUsesField.set(recipe, 10000);

            Field rewardExpField = MerchantRecipe.class.getDeclaredField("rewardExp");
            rewardExpField.setAccessible(true);
            rewardExpField.set(recipe, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipe;
    }

    protected MerchantRecipe createMerchantRecipe(org.bukkit.inventory.ItemStack item1, org.bukkit.inventory.ItemStack item3) {
        MerchantRecipe recipe = new MerchantRecipe(CraftItemStack.asNMSCopy(item1), CraftItemStack.asNMSCopy(item3));
        try {
            Field maxUsesField = MerchantRecipe.class.getDeclaredField("maxUses");
            maxUsesField.setAccessible(true);
            maxUsesField.set(recipe, 10000);

            Field rewardExpField = MerchantRecipe.class.getDeclaredField("rewardExp");
            rewardExpField.setAccessible(true);
            rewardExpField.set(recipe, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipe;
    }

    @Override
    public void appreciate(LivingUserDieEvent event) {
        Player player = event.getKiller().getPlayer();
        giveItem(player.getPlayer(), new ItemStack(Material.EMERALD, 8), player.getLocation());
    }

}
