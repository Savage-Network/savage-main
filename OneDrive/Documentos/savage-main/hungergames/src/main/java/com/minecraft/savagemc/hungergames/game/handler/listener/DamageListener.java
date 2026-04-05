/*
 * Copyright (C) Savage Network, Todos os direitos reservados
 * Este código é propriedade da Savage Network
 * Uso, distribuição ou modificação não autorizados são proibidos
 */

package com.minecraft.savagemc.hungergames.game.handler.listener;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.minecraft.savagemc.hungergames.util.constructor.Assistance;
import com.minecraft.savagemc.hungergames.util.constructor.listener.RecurringListener;
import com.minecraft.savagemc.hungergames.util.game.GameStage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

@RecurringListener(register = GameStage.PLAYING, unregister = GameStage.VICTORY)
public class DamageListener implements Listener, Assistance {

    /*

      Wooden Sword:
       Normal: 2.5 (5) -> 1.5 (3) [-2]
       Critical: 3 (6) -> 2 (4) [-2]

      Golden Sword:
       Normal: 2.5 (5) -> 1.5 (3) [-2]
       Critical: 3 (6) -> 2 (4) [-2]

      Stone Sword:
       Normal: 3 (6) -> 2 (4) [-2]
       Critical: 3.5 (7) 2.5 (5) [-2]

      Iron Sword:
       Normal: 3.5 (7) -> 2.5 (5) [-2]
       Critical: 4 (8) -> 3 (6) [-2]

      Diamond Sword:
       Normal: 4 (8) -> 3 (6) [-2]
       Critical: 4.5 (9) -> 3.5 (7) [-2]

     * Note: To fix damage is x - 2.

     */

    private final ImmutableSet<Material> ACCEPTABLE_MATERIALS = Sets.immutableEnumSet(Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLD_SWORD, Material.DIAMOND_SWORD, Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE, Material.WOOD_SPADE, Material.STONE_SPADE, Material.IRON_SPADE, Material.GOLD_SPADE, Material.DIAMOND_SPADE);

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if ((event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
            Player player = (Player) event.getDamager();
            ItemStack itemStack = player.getItemInHand();
            if (itemStack != null && ACCEPTABLE_MATERIALS.contains(itemStack.getType()))
                event.setDamage(Math.max(2, event.getDamage() - 3.5));
            else if (itemStack != null && itemStack.getType() == Material.MUSHROOM_SOUP)
                event.setDamage(1);
        }
    }

}
