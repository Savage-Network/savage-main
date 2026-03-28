package com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public class Snail extends Kit {

    public Snail() {
        super("Snail", Style.COMBAT, Item.of(Material.SOUL_SAND), 0,
                Arrays.asList("§7Cause lentidão no seu", "§7oponente."), new ArrayList<>());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSnail(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        if (isUsing(player) && Savage.RANDOM.nextInt(4) == 0)
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 0));
    }
}
