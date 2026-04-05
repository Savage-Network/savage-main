package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.bukkit.util.item.ItemFactory;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Milkman extends Kit {

    public Milkman(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.MILK_BUCKET));
        setItems(new ItemFactory(Material.MILK_BUCKET).setName("§aLeite Mágico").setDescription("§7Kit Milkman").getStack());
        setCooldown(40);
        setKitCategory(KitCategory.COMBAT);
        setPrice(25000);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (isItem(event.getItem())) {

            Player player = event.getPlayer();

            if (!isUser(player))
                return;

            event.setCancelled(true);
            player.setItemInHand(player.getItemInHand());

            if (isCooldown(player)) {
                dispatchCooldown(player);
                return;
            }

            addCooldown(player.getUniqueId());
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 500, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 500, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 500, 0));
            player.setFoodLevel(20);
        }
    }
}
