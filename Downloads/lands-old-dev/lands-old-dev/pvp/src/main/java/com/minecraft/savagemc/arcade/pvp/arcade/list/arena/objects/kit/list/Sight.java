package com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.list;

import com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.Kit;
import com.minecraft.savagemc.arcade.pvp.arcade.list.arena.objects.kit.enums.KitStyle;
import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class Sight extends Kit implements Listener {

    public Sight() {
        super("Sight", Item.of(Material.INK_SACK, 1), KitStyle.COMBAT,
                Arrays.asList("§7Tenha chance de deixar os",
                        "§7seus inimigos cegos."));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSight(PlayerDamageTargetEvent event) {
        Player target = event.getTarget(), damager = event.getPlayer();

        if (isUsingKit(damager) && isAllow(target)) {
            if (isNeo(target)) return;

            if (Savage.RANDOM.nextInt(5) == 2) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));

                target.getLocation().getWorld().playEffect(target.getLocation().clone().add(0.0, 0.4, 0.0), Effect.CLOUD, 159, 13);
            }
        }
    }
}
