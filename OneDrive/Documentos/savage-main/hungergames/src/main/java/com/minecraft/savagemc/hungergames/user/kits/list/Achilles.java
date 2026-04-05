package com.minecraft.savagemc.hungergames.user.kits.list;

import com.minecraft.core.Constants;
import com.minecraft.core.account.Account;
import com.minecraft.core.bukkit.util.worldedit.Pattern;
import com.minecraft.savagemc.hungergames.HungerGames;
import com.minecraft.savagemc.hungergames.user.kits.Kit;
import com.minecraft.savagemc.hungergames.user.kits.pattern.KitCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Achilles extends Kit {

    public Achilles(HungerGames hungerGames) {
        super(hungerGames);
        setIcon(Pattern.of(Material.WOOD_SWORD));
        setKitCategory(KitCategory.COMBAT);
        setPrice(25000);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player && e.getEntity() instanceof Player))
            return;

        final Player player = (Player) e.getEntity();

        if (isUser(player)) {

            final Player damager = (Player) e.getDamager();
            final ItemStack itemStack = damager.getItemInHand();

            if (itemStack != null && itemStack.getType().name().contains("WOOD_")) {
                double damage = e.getDamage() + 4.0D;

                boolean hasHelmet = (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.AIR);
                boolean hasChestplate = (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() != Material.AIR);
                boolean hasLeggings = (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().getType() != Material.AIR);
                boolean hasBoots = (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() != Material.AIR);

                if (hasHelmet) {
                    damage = damage + 1.5D;
                }

                if (hasChestplate) {
                    damage = damage + 2.0D;
                }

                if (hasLeggings) {
                    damage = damage + 2.0D;
                }

                if (hasBoots) {
                    damage = damage + 1.5D;
                }

                e.setDamage(damage);
            } else if (itemStack != null && !itemStack.getType().name().contains("WOOD_")) {
                e.setDamage(e.getDamage() - 2.0D);

                if (Constants.RANDOM.nextInt(8) == 0)
                    damager.sendMessage(Account.fetch(damager.getUniqueId()).getLanguage().translate("kit.achilles.warn"));
            }
        }
    }

}
