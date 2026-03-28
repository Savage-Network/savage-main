package com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.list;

import com.minecraft.savagemc.core.Savage;
import com.minecraft.savagemc.core.api.item.Item;
import com.minecraft.savagemc.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.savagemc.arcade.duels.arcade.list.combat.simulator.kit.model.Kit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;

public class Anchor extends Kit {

    public Anchor() {
        super("Anchor", Style.COMBAT, Item.of(Material.ANVIL), -1, Arrays.asList("§7Não dê, nem receba", "§7knockback."), new ArrayList<>());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnchor(PlayerDamageTargetEvent event) {
        Player player = event.getPlayer(), target = event.getTarget();

        if (isUsing(player) || isUsing(target)) {
            if (isInvincible(target)) return;

            handleKnockback(target);
        }
    }

    protected void handleKnockback(Player entity) {
        Savage.getPlatform().runSync(() -> {
            entity.setVelocity(new Vector(0, -1, 0));
            entity.playSound(Sound.ANVIL_USE);
        }, 1);
    }
}
